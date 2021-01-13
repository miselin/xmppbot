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
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.TextChannel;
import reactor.core.publisher.Mono;

/**
 * The main entry point and state of the bot's connection is managed here.
 *
 * @author Matthew Iselin <matthew@theiselins.net>
 */
public class XMPPBot {

    private GatewayDiscordClient connection_ = null;
    private boolean active_ = false;
    private String username_ = null;
    private String jid_ = null;
    private static final List<BaseCommand> commands_ = new ArrayList<>();

    /**
     * Connect and log in to Discord.
     *
     * @param token Discord token
     * @param username Username.
     * @throws IOException thrown on I/O errors e.g. with connecting.
     * @throws DiscordException thrown on Discord errors.
     */
    private void login(String token, String username) throws IOException {
        if (null != connection_) {
            System.err.println("Already connected!");
            return;
        }

        System.err.println("Logging in with token " + token + ".");

        GatewayDiscordClient client = DiscordClientBuilder.create(token)
                .build()
                .login()
                .block();

        client.getEventDispatcher().on(MessageCreateEvent.class)
                .map(MessageCreateEvent::getMessage)
                .filter(message -> message.getAuthor().map(user -> !user.isBot()).orElse(false))
                .flatMap(message -> handle(message))
                .subscribe();

        connection_ = client;
        active_ = true;
    }

    /**
     * Disconnect from the server and go inactive.
     */
    public void disconnect() {
        if (!isActive()) {
            return;
        }

        connection_.logout().block();
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

    public void waitForDisconnect() {
        if (null != connection_) {
            connection_.onDisconnect().block();
        }
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

        if (!props.containsKey("token")) {
            // Token not in the config file.
            // Try and read the token from the environment.
            String token = System.getenv("DISCORD_TOKEN");
            if (token != null) {
                props.setProperty("token", token);
            }
        }

        // Log in and immediately join the main multi-chat.
        b.login(props.getProperty("token", ""),
                props.getProperty("username", "skynet"));

        // Stay alive until we're asked to terminate.
        b.waitForDisconnect();
    }

    public Mono<Message> handle(Message msg) {
        if (!isActive()) {
            return null;
        }

        TextChannel channel = msg.getChannel().ofType(TextChannel.class).block();

        // TODO: figure out private messages in Discord4J 3.x
        if (null == channel) {

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
            String from = msg.getAuthor().get().getUsername();
            String[] responses = getResponses(from, msg.getContent());
            if (null != responses) {
                boolean first = true;
                String final_response = msg.getAuthor().get().getMention() + " ";
                for (String response : responses) {
                    if (!first) {
                        final_response += "\n";
                    } else {
                        first = false;
                    }

                    final_response += response;
                }

                return channel.createMessage(final_response);
            }
        }

        return null;
    }

}
