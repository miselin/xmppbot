
package org.miselin.xmppbot;

/**
 * ShutdownCommand is an unusual command in that it accesses the main XMPPBot instance. The command
 * shuts down the bot cleanly.
 *
 * @author Matthew Iselin <matthew@theiselins.net>
 */
public class ShutdownCommand implements BaseCommand {

  private XMPPBot bot_ = null;

  public ShutdownCommand(XMPPBot bot) {
    bot_ = bot;
  }

  @Override
  public String usage() {
    return "";
  }

  @Override
  public String description() {
    return "shut down the bot gracefully (only for admins).";
  }

  @Override
  public String token() {
    return "shutdown";
  }

  @Override
  public String[] handle(String message, String from) {
    String response;
    if (bot_.isAdminUser(from)) {
      bot_.setActive(false);
      response = "shutting down";
    } else {
      response = "you are not an admin";
    }

    return new String[]{response};
  }

}
