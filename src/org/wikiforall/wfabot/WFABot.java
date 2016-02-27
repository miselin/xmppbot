
package org.wikiforall.wfabot;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jxmpp.util.XmppStringUtils;

/**
 * The main entry point and state of the bot's connection is managed here.
 *
 * @author Matthew Iselin <matthew@theiselins.net>
 */
public class WFABot implements ChatMessageListener, ChatManagerListener {

  private XMPPTCPConnection connection_ = null;
  private ChatManager chatmanager_ = null;
  private MultiUserChatManager multichatmanager_ = null;
  private boolean active_ = false;
  private String username_ = null;
  private String jid_ = null;
  private static final List<BaseCommand> commands_ = new ArrayList<>();

  /**
   * Connect and log in to the given XMPP host on port 5222.
   *
   * @param hostname host to connect to (DNS or IP address).
   * @param service XMPP service.
   * @param username XMPP server username.
   * @param password XMPP server password.
   * @throws SmackException thrown on Smack API errors.
   * @throws IOException thrown on I/O errors e.g. with connecting.
   * @throws XMPPException thrown on XMPP protocol errors.
   */
  private void login(String hostname, String service, String username, String password) throws SmackException, IOException, XMPPException {
    if (null != connection_) {
      System.err.println("Already connected!");
      return;
    }

    username_ = username;
    XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
            .setUsernameAndPassword(username, password)
            .setServiceName(service)
            .setHost(hostname)
            .setPort(5222)
            .setResource("")
            .setSendPresence(true)
            .setDebuggerEnabled(false)
            .setSecurityMode(ConnectionConfiguration.SecurityMode.ifpossible)
            .build();

    connection_ = new XMPPTCPConnection(config);
    connection_.connect();
    connection_.login();

    jid_ = XmppStringUtils.parseBareJid(connection_.getUser());

    chatmanager_ = ChatManager.getInstanceFor(connection_);
    multichatmanager_ = MultiUserChatManager.getInstanceFor(connection_);
    chatmanager_.addChatListener(this);

    active_ = true;
  }

  /**
   * Join the given multi-user chat room.
   *
   * @param room The name of the room to join.
   * @return a new MultiUserChat object for the joined chat.
   */
  private MultiUserChat joinRoom(String room) throws SmackException.NoResponseException, XMPPException.XMPPErrorException, SmackException.NotConnectedException {
    if (!isActive()) {
      return null;
    }

    final MultiUserChat muc = multichatmanager_.getMultiUserChat(room);

    // No history on join.
    DiscussionHistory history = new DiscussionHistory();
    history.setMaxStanzas(0);
    muc.join(username_, "", history, connection_.getPacketReplyTimeout());
    muc.addMessageListener(new MessageListener() {
      @Override
      public void processMessage(Message message) {
        handleCommand(null, muc, message);
      }
    });
    return muc;
  }

  /**
   * Disconnect from the server and go inactive.
   */
  public void disconnect() {
    if (!isActive()) {
      return;
    }

    connection_.disconnect();
    connection_ = null;
    active_ = false;
  }

  /**
   * Checks that the given user has admin rights.
   *
   * @param username the username (NOT a JID).
   * @return true if the user is an admin, false otherwise.
   */
  public boolean isAdminUser(String username) {
    System.err.println("isAdminUser(" + username + ")");
    switch (username) {
      case "miselin":
      case "shadylives":
        return true;
      default:
        return false;
    }
  }

  /**
   * Checks that the given user has admin rights.
   *
   * @param jid the user's JID (e.g. foo@bar.net/resource)
   * @return true if the user is an admin, false otherwise.
   */
  public boolean isAdmin(String jid) {
    String resource = XmppStringUtils.parseResource(jid);
    String localpart = XmppStringUtils.parseLocalpart(jid);
    return isAdminUser(resource) || isAdminUser(localpart);
  }

  /**
   * Gets responses for the given message from the given person.
   *
   * @param from the person from which the message was sent
   * @param body the body of the message
   * @return array of messages to send back to the user or chat
   */
  public String[] getResponses(String from, String body) {
    if (body.startsWith("!")) {
      String which = body.split(" ")[0];
      for (BaseCommand cmd : commands_) {
        if (("!" + cmd.token()).equals(which)) {
          return cmd.handle(body, from);
        }
      }
    }

    return new String[]{};
  }

  /**
   * Handles a command on a private chat or on a multi-user chat.
   *
   * @param chat if not-null, this is a private chat; instance used to respond
   * @param muc if not-null, this is a multi-user chat; instance used to respond
   * @param incoming the incoming message
   */
  public void handleCommand(Chat chat, MultiUserChat muc, Message incoming) {
    if (!isActive()) {
      return;
    }

    if (null == incoming.getBody()) {
      return;
    }

    if (null != chat) {
      // Private message. But only care if it's to us directly.
      if (XmppStringUtils.parseBareJid(incoming.getTo()).equals(jid_)) {
        // For private chats, the local part is the user's identity.
        String[] responses = getResponses(XmppStringUtils.parseLocalpart(incoming.getFrom()), incoming.getBody());
        if (null != responses) {
          try {
            for (String response : responses) {
              chat.sendMessage(response);
            }
          } catch (SmackException.NotConnectedException ex) {
            Logger.getLogger(WFABot.class.getName()).log(Level.SEVERE, null, ex);
          }
        }
      }
    } else if (null != muc) {
      // For MUCs, the resource is the user's identity.
      String from = XmppStringUtils.parseResource(incoming.getFrom());
      String[] responses = getResponses(from, incoming.getBody());
      if (null != responses) {
        try {
          for (String response : responses) {
            muc.sendMessage(from + ": " + response);
          }
        } catch (SmackException.NotConnectedException ex) {
          Logger.getLogger(WFABot.class.getName()).log(Level.SEVERE, null, ex);
        }
      }
    }
  }

  /**
   * Process an incoming message in a private conversation (ChatMessageListener interface)
   *
   * @param chat the private chat instance
   * @param message the message that was received
   */
  @Override
  public void processMessage(Chat chat, Message message) {
    handleCommand(chat, null, message);
  }

  /**
   * Handle someone creating a chat with us. We need to install ourselves as a listener on it so we
   * actually get the messages.
   *
   * @param chat the private chat instance
   * @param createdLocally whether the chat was created by us or by someone else
   */
  @Override
  public void chatCreated(Chat chat, boolean createdLocally) {
    // Make sure we handle all messages in this chat.
    if (!createdLocally) {
      chat.addMessageListener(this);
    }
  }

  /**
   * Determines whether the bot is currently active.
   *
   * @return true if active, false otherwise
   */
  public boolean isActive() {
    return active_ && (null != connection_);
  }

  /**
   * Sets the active state to request a termination without immediate disconnect.
   *
   * @param b the new active state
   */
  public void setActive(boolean b) {
    active_ = b;
  }

  public Properties getProperties(String pathname) {
    Properties props = new Properties();

    if (null != pathname) {
      InputStream input = null;
      try {
        // Load from the given file if we can.
        input = new FileInputStream(pathname);
      } catch (FileNotFoundException ex) {
        // No dice.
        System.err.println("Cannot open config path " + pathname);
        return props;
      }

      // Load from the file.
      try {
        props.load(input);
      } catch (IOException ex) {
        System.err.println("Loading properties from " + pathname + " failed, using defaults.");
      }
    }

    return props;
  }

  public static void main(String[] args) throws Throwable {
    WFABot b = new WFABot();

    // Register known commands.
    commands_.add(new DiceCommand());
    commands_.add(new ForexCommand());
    commands_.add(new StockCommand());
    commands_.add(new StockNDayCommand());
    commands_.add(new ShutdownCommand(b));
    commands_.add(new PingCommand());

    // Load configuration.
    String props_path = null;
    if (args.length > 0) {
      props_path = args[0];
    }
    Properties props = b.getProperties(props_path);

    // Log in and immediately join the main multi-chat.
    b.login(props.getProperty("hostname", "localhost"),
            props.getProperty("service", "wikiforall.net"),
            props.getProperty("username", "skynet"),
            props.getProperty("password", "Q()@UN!%AO42L61CUF#(AAS23D"));
    MultiUserChat muc = b.joinRoom("general@multi.wikiforall.net");

    // Stay alive until we're asked to terminate.
    while (b.isActive()) {
      Thread.sleep(1000);
    }

    // Clean termination.
    b.disconnect();
  }

}
