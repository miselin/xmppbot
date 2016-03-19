
package org.miselin.xmppbot;

import java.util.ArrayList;
import java.util.List;
import org.miselin.xmppbot.util.PersistentStorage;
import org.miselin.xmppbot.util.StringUtils;

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
    return "[add TICKER1 [TICKER2 [.. TICKERN]]|del TICKER1 [TICKER2 [.. TICKERN]]|show USER]";
  }

  @Override
  public String description() {
    return "add or remove the given ticker from your watchlist, pass nothing to list your watchlist";
  }

  private String[] showTickers(String forwho) {
    StockCommand cmd = new StockCommand();
    List<String> tickers = (List<String>) PersistentStorage.get("watchlist-" + forwho);
    if (null == tickers) {
      return new String[]{String.format("no tickers for %s yet", forwho)};
    }

    List<StockCommand.StockQuote> quotes = cmd.getQuotes(tickers);
    String message = String.format("your watchlist (ticker, last close, last trade):");
    for (StockCommand.StockQuote quote : quotes) {
      message += String.format(" %s %.3f/%.3f/%s",
              quote.ticker, quote.close, quote.lasttrade,
              StringUtils.arrow(quote.close, quote.lasttrade));
    }
    return new String[]{message};
  }

  @Override
  public String[] handle(String message, String from) {
    StockCommand cmd = new StockCommand();

    String[] split = message.split(" ");
    if (split.length == 1) {
      // List only.
      return showTickers(from);
    } else if (split.length >= 3) {
      List<String> tickers = (List<String>) PersistentStorage.get("watchlist-" + from);
      if (null == tickers) {
        tickers = new ArrayList<>();
      }

      boolean ok = true;
      switch (split[1]) {
        case "add":
          for (int i = 2; i < split.length; i++) {
            tickers.add(cmd.fixTicker(split[i]));
          }
          break;
        case "del":
          for (int i = 2; i < split.length; i++) {
            tickers.remove(cmd.fixTicker(split[i]));
          }
          break;
        case "show":
          return showTickers(split[2]);
        default:
          ok = false;
          break;
      }

      if (ok) {
        PersistentStorage.set("watchlist-" + from, tickers);
        return showTickers(from);
      }
    }

    return new String[]{"unknown/insufficient parameters for watchlist command"};
  }

}
