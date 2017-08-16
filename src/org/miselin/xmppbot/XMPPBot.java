package org.miselin.xmppbot;

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
import org.jivesoftware.smack.ReconnectionManager;
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
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.util.DiscordException;

/**
 * The main entry point and state of the bot's connection is managed here.
 *
 * @author Matthew Iselin <matthew@theiselins.net>
 */
public class XMPPBot implements IListener<MessageReceivedEvent> {

    private IDiscordClient connection_ = null;
    private boolean active_ = false;
    private String username_ = null;
    private String jid_ = null;
    private static final List<BaseCommand> commands_ = new ArrayList<>();

    /**
     * Connect and log in to Discord.
     *
     * @param token Discord token
     * @param username Username.
     * @throws SmackException thrown on Smack API errors.
     * @throws IOException thrown on I/O errors e.g. with connecting.
     * @throws XMPPException thrown on XMPP protocol errors.
     */
    private void login(String token, String username) throws IOException, DiscordException {
        if (null != connection_) {
            System.err.println("Already connected!");
            return;
        }

        System.err.println("Logging in with token " + token + ".");
        ClientBuilder clientBuilder = new ClientBuilder();
        clientBuilder.withToken(token);

        connection_ = clientBuilder.login();
        connection_.getDispatcher().registerListener(this);

        active_ = true;
    }

    /**
     * Disconnect from the server and go inactive.
     */
    public void disconnect() {
        if (!isActive()) {
            return;
        }

        connection_.logout();
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
     * Handles the !help command (this is a special command).
     *
     * @return set of messages with usage instructions
     */
    public String[] getHelp() {
        List<String> entries = new ArrayList<>();
        for (BaseCommand cmd : commands_) {
            String usage = cmd.usage();
            if (!usage.isEmpty()) {
                usage = " " + usage;
            }
            entries.add(String.format("-> !%s%s: %s", cmd.token(), usage, cmd.description()));
        }
        return entries.toArray(new String[entries.size()]);
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
            if (which.equals("!help")) {
                return getHelp();
            }

            for (BaseCommand cmd : commands_) {
                if (("!" + cmd.token()).equals(which)) {
                    return cmd.handle(body, from);
                }
            }
        }

        return new String[]{};
    }

    public String getUsername() {
        return username_;
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
     * Sets the active state to request a termination without immediate
     * disconnect.
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
        XMPPBot b = new XMPPBot();

        // Register known commands.
        commands_.add(new IsItUpCommand());
        commands_.add(new PyDocCommand());
        commands_.add(new DiceCommand());
        commands_.add(new ForexCommand());
        commands_.add(new SpotifyCommand());
        commands_.add(new StockCommand());
        commands_.add(new StockNDayCommand());
        commands_.add(new ShutdownCommand(b));
        commands_.add(new PingCommand());
        commands_.add(new WatchlistCommand());

        // Load configuration.
        String props_path = null;
        if (args.length > 0) {
            props_path = args[0];
        }
        Properties props = b.getProperties(props_path);

        // Log in and immediately join the main multi-chat.
        b.login(props.getProperty("token", ""),
                props.getProperty("username", "skynet"));

        // Stay alive until we're asked to terminate.
        while (b.isActive()) {
            Thread.sleep(1000);
        }

        // Clean termination.
        b.disconnect();
    }

    @Override
    public void handle(MessageReceivedEvent event) {
        if (!isActive()) {
            return;
        }

        if (event.getChannel().isPrivate()) {

            /*
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
                        Logger.getLogger(XMPPBot.class
                                .getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
             */
            // TODO: IMPLEMENT ME
        } else {
            String from = event.getAuthor().getName();
            String[] responses = getResponses(from, event.getMessage().getContent());
            if (null != responses) {
                try {
                    for (String response : responses) {
                        event.getChannel().sendMessage(event.getAuthor().mention() + " " + response);

                    }
                } catch (DiscordException ex) {
                    Logger.getLogger(XMPPBot.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

}
