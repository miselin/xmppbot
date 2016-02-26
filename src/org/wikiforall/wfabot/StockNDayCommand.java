
package org.wikiforall.wfabot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * StockNDayCommand provides a way of getting historical data for a single stock.
 *
 * @author Matthew Iselin <matthew@theiselins.net>
 */
public class StockNDayCommand extends StockCommand {

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

      // Allow overriding # of days for history.
      // TODO(miselin): extend this to support different granularities e.g. weekly, monthly.
      if (entry.contains("*")) {
        String[] split = entry.split("\\*");
        entry = split[0];
        days = Integer.parseInt(split[1]);
      }

      // Convert to the right type of ticker for our use (e.g. ASX)
      entry = fixTicker(entry);
      tickers.add(entry);
    }

    if (tickers.isEmpty()) {
      return new String[]{"no tickers were given (usage: '!stocknday CODE[*days]' - defaults to 5 days)"};
    }

    String s = "http://ichart.finance.yahoo.com/table.csv?s=" + tickers.get(0);

    List<String> closes = new ArrayList<>();
    List<HistoricalData> history = new ArrayList<>();

    String csv = Downloader.download(s);
    List<String> lines = Arrays.asList(csv.split("\n"));
    Double last_close = null;
    for (String line : lines) {
      // Remove quotes, company names and symbols won't have commas (maybe).
      line = line.trim().replace("\"", "");
      if (line.isEmpty()) {
        continue;
      }

      // Terrible hack
      if (line.startsWith("Date")) {
        continue;
      }

      String[] fields = line.split(",");
      if (fields.length < 5) {
        continue;
      }

      // Only care about its close price.
      HistoricalData hist = new HistoricalData();
      hist.high = Double.parseDouble(fields[2]);
      hist.low = Double.parseDouble(fields[3]);
      hist.close = Double.parseDouble(fields[4]);
      history.add(hist);

      if (history.size() >= days) {
        break;
      }
    }

    // Rearrange so we present from the oldest data to the newest.
    Collections.reverse(history);

    // Stringify each data point.
    for (HistoricalData entry : history) {
      String arrow;
      if (last_close != null) {
        if (entry.close > last_close) {
          arrow = " ↑";
        } else if (entry.close == last_close) {
          arrow = " =";
        } else {
          arrow = " ↓";
        }
      } else {
        arrow = "";
      }
      closes.add(String.format("%.3f %.3f %.3f%s", entry.low, entry.high, entry.close, arrow));

      last_close = entry.close;
    }

    String message = String.format("%d days of %s (low/high/close): ", days, tickers.get(0));
    for (int i = 0; i < closes.size(); i++) {
      message += closes.get(i);
      if ((i + 1) < closes.size()) {
        message += ", ";
      }
    }
    return new String[]{message};
  }

}
