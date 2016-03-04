
package org.miselin.xmppbot;

import java.io.IOException;
import org.miselin.xmppbot.util.Downloader;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 * StockCommand provides a command to retrieve a stock quote for one or more stocks.
 *
 * @author Matthew Iselin <matthew@theiselins.net>
 */
public class StockCommand implements BaseCommand {

  public class StockQuote {

    public String ticker;
    public String name;
    public double ask;
    public double bid;
    public double close;
    public double open;
  }

  /**
   * Build a StockQuote object from the given Yahoo Finance CSV record.
   *
   * @param record the record to build from
   * @return a constructed StockQuote filled out from the CSV record.
   */
  private StockQuote fromRecord(CSVRecord record) {
    StockQuote quote = new StockQuote();
    quote.ticker = record.get(0);
    quote.name = record.get(1);
    quote.ask = Double.parseDouble(record.get(2));
    quote.bid = Double.parseDouble(record.get(3));
    quote.close = Double.parseDouble(record.get(4));
    quote.open = Double.parseDouble(record.get(5));
    return quote;
  }

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

  public List<StockQuote> getQuotes(List<String> tickers) {
    StringBuilder s = new StringBuilder("http://finance.yahoo.com/d/quotes.csv?s=");
    for (String ticker : tickers) {
      s.append(ticker).append("+");
    }
    // ticker, name, ask, bid, previous close, open
    s.append("&f=snabpo");

    List<String> messages = new ArrayList<>();

    String csv = Downloader.download(s.toString());

    CSVParser parser;
    try {
      parser = CSVParser.parse(csv, CSVFormat.RFC4180.withNullString("N/A"));
    } catch (IOException ex) {
      return null;
    }

    List<StockQuote> result = new ArrayList<>();
    for (CSVRecord record : parser) {
      StockQuote q = fromRecord(record);
      if (null == q) {
        continue;
      }
      result.add(q);
    }

    return result;
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

    List<StockQuote> quotes = getQuotes(tickers);
    if (null == quotes) {
      return new String[]{"Yahoo Finance gave a malformed response."};
    }

    List<String> messages = new ArrayList<>();

    for (StockQuote sq : quotes) {
      messages.add(String.format("%s (%s) ask %.3f bid %.3f open %.3f close %.3f",
              sq.ticker, sq.name, sq.ask, sq.bid, sq.close, sq.open));
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
