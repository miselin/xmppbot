
package org.wikiforall.wfabot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Downloader provides convenience methods for downloading content.
 *
 * These tend to ignore errors and return null if things go south, which is perfectly fine for most
 * of the use cases that exist in the bot.
 *
 * @author Matthew Iselin <matthew@theiselins.net>
 */
public class Downloader {

  public static String download(String theurl) {
    URL url;
    try {
      url = new URL(theurl);
    } catch (MalformedURLException ex) {
      return null;
    }
    HttpURLConnection http;
    int response;
    InputStream ins;
    try {
      http = (HttpURLConnection) url.openConnection();
      response = http.getResponseCode();
      if (response != HttpURLConnection.HTTP_OK) {
        return null;
      }

      ins = http.getInputStream();
    } catch (IOException ex) {
      return null;
    }

    ByteArrayOutputStream outs = new ByteArrayOutputStream();
    String result = "";
    int b = -1;
    byte[] buffer = new byte[512];
    try {
      while ((b = ins.read(buffer)) != -1) {
        outs.write(buffer);
      }
      ins.close();
    } catch (IOException ex) {
      return null;
    }
    http.disconnect();

    return outs.toString();
  }
}
