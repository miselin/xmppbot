
package org.miselin.xmppbot;

/**
 * ForexCommand provides a way of getting currency conversions quickly and easily.
 *
 * It uses the same financial data as the base StockCommand, but has a modified ticker fix.
 *
 * @author Matthew Iselin <matthew@theiselins.net>
 */
public class ForexCommand extends StockCommand {

  @Override
  public String usage() {
    return "ticker";
  }

  @Override
  public String description() {
    return "get a currency exchange quote for the given currency pair (e.g. USDAUD).";
  }

  @Override
  public String fixTicker(String ticker) {
    return ticker + "=X";
  }

  @Override
  public String token() {
    return "forex";
  }

}