
package org.miselin.xmppbot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Allows simple inbox functionality between (potentially) offline participants.
 *
 * @author James de Vries <ministryofwin@gmail.com>
 */
public class OfflineMessageCommand implements BaseCommand {

  @Override
  public String usage() {
    return "[send|list|show <number>]";
  }

  @Override
  public String description() {
    return "lorem ipsum";
  }

  @Override
  public String token() {
    return "mail";
  }

  @Override
  public String[] handle(String message, String from) {
    return new String[]{"pong"};
  }

}