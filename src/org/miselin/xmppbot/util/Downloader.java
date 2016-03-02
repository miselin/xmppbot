
package org.miselin.xmppbot.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.cache.HttpCacheContext;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.cache.CachingHttpClients;

/**
 * Downloader provides convenience methods for downloading content.
 *
 * These tend to ignore errors and return null if things go south, which is perfectly fine for most
 * of the use cases that exist in the bot.
 *
 * @author Matthew Iselin <matthew@theiselins.net>
 */
public class Downloader {

  private static CloseableHttpClient client_ = null;

  private static void initialize() {
    if (null != client_) {
      return;
    }

    // Build our caching client; in-memory suffices here (for the purposes of this bot, anyway).
    client_ = CachingHttpClients.createMemoryBound();
  }

  public static String download(String theurl) {
    initialize();

    HttpCacheContext context = HttpCacheContext.create();
    HttpGet httpget = new HttpGet(theurl);
    CloseableHttpResponse response;
    try {
      response = client_.execute(httpget, context);
    } catch (IOException ex) {
      Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }

    if (response.getStatusLine().getStatusCode() != 200) {
      try {
        // Bad response.
        response.close();
      } catch (IOException ex) {
        // Doesn't really matter, already about to exit the function.
        Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
      }
      return null;
    }

    try {
      InputStream ins = response.getEntity().getContent();

      StringWriter writer = new StringWriter();
      IOUtils.copy(ins, writer, Charset.forName("UTF-8"));
      String result = writer.toString();

      response.close();

      return result;
    } catch (IOException ex) {
      // Doens't really matter.
      Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
      return null;
    }
  }
}
