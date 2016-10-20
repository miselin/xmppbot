
package org.miselin.xmppbot;

import java.io.IOException;
import org.miselin.xmppbot.util.Downloader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.miselin.xmppbot.util.StringUtils;

/**
 * StockNDayCommand provides a way of getting historical data for a single stock.
 *
 * @author Matthew Iselin <matthew@theiselins.net>
 */
public class StockNDayCommand extends StockCommand {

  @Override
  public String usage() {
    return "ticker[*N]";
  }

  @Override
  public String description() {
    return "get N days worth of stock price data for the given ticker (N defaults to 5).";
  }

  private class HistoricalData {
    public double high;
    public double low;
    public double close;
  }

  @Override
  public String token() {
    return "hstock";
  }

  @Override
  public String[] handleStockCommand(String msg) {
    int days = 5;
    String[] entries = msg.split(" ");
    List<String> tickers = new ArrayList<>();
    for (String entry : entries) {
      if (entry.startsWith("!")) {
        // Ignore (can be used for e.g. extra context)
        continue;
      }

      tickers.add(entry);
    }

    if (tickers.isEmpty()) {
      return new String[]{"no tickers were given (usage: '!stocknday CODE[*days]' - defaults to 5 days)"};
    }

    List<String> messages = new ArrayList<>();

    for (String ticker : tickers) {
      // Allow overriding # of days for history.
      // TODO(miselin): extend this to support different granularities e.g. weekly, monthly.
      if (ticker.contains("*")) {
        String[] split = ticker.split("\\*");
        ticker = split[0];
        days = Integer.parseInt(split[1]);
      }

      // Convert to the right type of ticker for our use (e.g. ASX)
      ticker = fixTicker(ticker);

      String s = "http://ichart.finance.yahoo.com/table.csv?s=" + ticker;

      List<String> closes = new ArrayList<>();
      List<HistoricalData> history = new ArrayList<>();

      String csv = Downloader.download(s);

      if (csv == null) {
        // TODO(miselin): uh, probably should log or tell the user or something?
        continue;
      }

      CSVParser parser;
      try {
        parser = CSVParser.parse(csv, CSVFormat.RFC4180.withNullString("N/A").withHeader());
      } catch (IOException ex) {
        return new String[]{"Yahoo Finance gave a malformed response."};
      }

      for (CSVRecord record : parser) {
        if (record.getRecordNumber() > days) {
          break;
        }

        // Only care about its close price.
        HistoricalData hist = new HistoricalData();
        hist.high = Double.parseDouble(record.get("High"));
        hist.low = Double.parseDouble(record.get("Low"));
        hist.close = Double.parseDouble(record.get("Close"));
        history.add(hist);
      }

      // Rearrange so we present from the oldest data to the newest.
      Collections.reverse(history);

      // Stringify each data point.
      Double last_close = null;
      for (HistoricalData entry : history) {
        String arrow;
        if (last_close != null) {
          arrow = StringUtils.arrow(last_close, entry.close);
        } else {
          arrow = "";
        }
        closes.add(String.format("%.3f %.3f %.3f%s", entry.low, entry.high, entry.close, arrow));

        last_close = entry.close;
      }

      messages.add(String.format("%d days of %s (low/high/close), oldest first:", days, ticker));
      for (int i = 0; i < closes.size(); i++) {
        messages.add(closes.get(i));
      }
    }
    return messages.toArray(new String[messages.size()]);
  }

}
