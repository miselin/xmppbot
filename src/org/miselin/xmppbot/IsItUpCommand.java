
package org.miselin.xmppbot;

import org.miselin.xmppbot.util.Downloader;
import java.util.ArrayList;
import java.util.List;

/**
 * Tells the user whether a particular page is up or not.
 *
 * @author Matthew Iselin <matthew@theiselins.net>
 */
public class IsItUpCommand implements BaseCommand {

  @Override
  public String usage() {
    return "[url]+";
  }

  @Override
  public String description() {
    return "check if the given URL is up or not (multiple can be provided).";
  }

  @Override
  public String token() {
    return "isitup";
  }

  @Override
  public String[] handle(String message, String from) {
    List<String> results = new ArrayList<>();
    for (String url : message.split(" ")) {
      if (url.startsWith("!")) {
        continue;
      }

      if (!url.startsWith("http")) {
        url = "http://" + url;
      }

      String result = Downloader.download(url);
      if (result == null) {
        results.add(url + " appears to be down");
      } else {
        results.add(url + " appears to be up");
      }
    }

    return results.toArray(new String[results.size()]);
  }

}
