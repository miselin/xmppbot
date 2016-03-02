
package org.miselin.xmppbot;

import org.miselin.xmppbot.util.Downloader;
import java.util.ArrayList;
import java.util.List;

/**
 * StockCommand provides a command to retrieve a stock quote for one or more stocks.
 *
 * @author Matthew Iselin <matthew@theiselins.net>
 */
public class StockCommand implements BaseCommand {

  @Override
  public String usage() {
    return "[ticker]+";
  }

  @Override
  public String description() {
    return "get a stock quote for the given ticker(s).";
  }

  public String fixTicker(String ticker) {
    // Lock to ASX by default.
    if (!ticker.contains(".")) {
      ticker += ".AX";
    } else {
      if (ticker.endsWith(".")) {
        ticker = ticker.substring(0, ticker.length() - 1);
      }
    }
    return ticker;
  }

  public String[] handleStockCommand(String msg) {
    System.out.println("handling stock command: " + msg);
    String[] entries = msg.split(" ");
    List<String> tickers = new ArrayList<>();
    for (String entry : entries) {
      if (entry.startsWith("!")) {
        // Ignore (can be used for e.g. extra context)
        continue;
      }

      entry = fixTicker(entry);
      System.out.println("adding ticker " + entry);
      tickers.add(entry);
    }

    if (tickers.isEmpty()) {
      return new String[]{"no tickers were given (usage: '!stock CODE1 CODE2 .. CODEN')"};
    }

    StringBuilder s = new StringBuilder("http://finance.yahoo.com/d/quotes.csv?s=");
    for (String ticker : tickers) {
      s.append(ticker).append("+");
    }
    // ticker, name, ask, bid, previous close, open
    s.append("&f=snabpo");

    System.out.println("built URL: " + s.toString());

    List<String> messages = new ArrayList<>();

    String csv = Downloader.download(s.toString());
    String[] lines = csv.split("\n");
    for (String line : lines) {
      line = line.trim().replace("\"", "");
      if (line.isEmpty()) {
        continue;
      }

      String[] fields = line.split(",");

      System.out.println(line);

      String ticker = fields[0];
      String name = fields[1];
      double ask = Double.parseDouble(fields[2]);
      double bid = Double.parseDouble(fields[3]);
      double close = Double.parseDouble(fields[4]);
      double open = Double.parseDouble(fields[5]);

      messages.add(String.format("%s (%s) ask %.3f bid %.3f open %.3f close %.3f",
              ticker, name, ask, bid, close, open));
    }

    return messages.toArray(new String[messages.size()]);
  }

  @Override
  public String token() {
    return "stock";
  }

  @Override
  public String[] handle(String message, String from) {
    return handleStockCommand(message);
  }
}
