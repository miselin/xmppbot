
package org.wikiforall.wfabot;

/**
 * PingCommand simply sends a pong to the requesting user. Useful for basic verification that the
 * bot is still communicating correctly.
 *
 * @author Matthew Iselin <matthew@theiselins.net>
 */
public class PingCommand implements BaseCommand {

  @Override
  public String token() {
    return "ping";
  }

  @Override
  public String[] handle(String message, String from) {
    return new String[]{"pong"};
  }

}
