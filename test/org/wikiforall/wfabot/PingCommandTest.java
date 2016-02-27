
package org.wikiforall.wfabot;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests the '!ping' command.
 *
 * @author Matthew Iselin <matthew@theiselins.net>
 */
public class PingCommandTest {

  public PingCommandTest() {
  }

  /**
   * Test of token method, of class PingCommand.
   */
  @Test
  public void testToken() {
    PingCommand instance = new PingCommand();
    String expResult = "ping";
    String result = instance.token();
    assertEquals("token is still 'ping'", expResult, result);
  }

  /**
   * Test of handle method, of class PingCommand.
   */
  @Test
  public void testHandle() {
    String message = "!ping";
    PingCommand instance = new PingCommand();
    String[] expResult = new String[]{"pong"};
    String[] result = instance.handle(message, "");
    assertArrayEquals("pong is sent correctly", expResult, result);
  }

}
