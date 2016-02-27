
package org.wikiforall.wfabot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Gets python function signatures and docstrings where available.
 *
 * @author Matthew Iselin <matthew@theiselins.net>
 */
public class PyDocCommand implements BaseCommand {

  @Override
  public String token() {
    return "pydoc";
  }

  @Override
  public String[] handle(String message, String from) {
    List<String> entries = new ArrayList<>();
    List<String> result = new ArrayList<>();
    for (String field : message.split(" ")) {
      if (field.startsWith("!")) {
        continue;
      }

      entries.add(field);
    }

    Runtime rt = Runtime.getRuntime();
    for (String entry : entries) {
      Process pr;
      try {
        pr = rt.exec("./scripts/sig.py " + entry);
      } catch (IOException ex) {
        Logger.getLogger(PyDocCommand.class.getName()).log(Level.SEVERE, null, ex);
        continue;
      }

      try {
        if (pr.waitFor() != 0) {
          // Broken, possibly not found, etc
          result.add("failed to find signature for '" + entry + "'");
          continue;
        }
      } catch (InterruptedException ex) {
        Logger.getLogger(PyDocCommand.class.getName()).log(Level.SEVERE, null, ex);
        result.add("interruption while finding signature for '" + entry + "'");
        continue;
      }

      BufferedReader r = new BufferedReader(new InputStreamReader(pr.getInputStream()));
      try {
        result.add(r.readLine());
        r.close();
      } catch (IOException ex) {
        Logger.getLogger(PyDocCommand.class.getName()).log(Level.SEVERE, null, ex);
        result.add("couldn't read output for '" + entry + "'");
      }
    }

    return result.toArray(new String[result.size()]);
  }

}
