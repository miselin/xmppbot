/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wikiforall.wfabot;

import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.mockito.Mockito;

/**
 *
 * @author New
 */
public class DiceCommandTest {

  DiceCommand instance_;

  public DiceCommandTest() {
  }

  @Before
  public void before() {
    instance_ = Mockito.spy(new DiceCommand());
  }

  /**
   * Test of handle method, of class DiceCommand.
   */
  @Test
  public void testHandle() {
    Mockito.doReturn(3).when(instance_).roll(6);

    String message = "!roll";
    String[] expResult = new String[]{"rolled a 3"};
    String[] result = instance_.handle(message, "");
    assertArrayEquals("rolling once works as expected", expResult, result);
  }

}
