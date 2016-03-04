
package org.miselin.xmppbot;

/**
 * Provides a mechanism for getting a set of stock quotes for a user's watchlist.
 *
 * @author Matthew Iselin <matthew@theiselins.net>
 */
public class WatchlistCommand implements BaseCommand {

  @Override
  public String token() {
    return "watchlist";
  }

  @Override
  public String usage() {
    return "nothing yet";
  }

  @Override
  public String description() {
    return "nothing yet";
  }

  @Override
  public String[] handle(String message, String from) {
    // TODO(miselin): pass args, add tickers to PersistentStorage, get quotes, etc
    return new String[]{"nothing to do here yet"};
  }

}
