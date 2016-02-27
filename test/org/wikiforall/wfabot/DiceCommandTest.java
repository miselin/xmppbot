/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wikiforall.wfabot;

import java.security.SecureRandom;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Before;
import org.mockito.Mockito;
import org.wikiforall.wfabot.DiceCommand.Dice;

/**
 *
 * @author New
 */
public class DiceCommandTest {

  /**
   * Test of parseDice method, of class DiceCommand.
   */
  @Test
  public void testParseDice() {
    DiceCommand cmd = new DiceCommand();
    List<Dice> dice = cmd.parseDice("1d20 2d10 3d6 d6 1d d");
    Dice[] dice_array = dice.toArray(new Dice[dice.size()]);

    Dice[] expected = new Dice[]{
      // 1d20
      cmd.new Dice(20),
      // 2d10
      cmd.new Dice(10),
      cmd.new Dice(10),
      // 3d6
      cmd.new Dice(6),
      cmd.new Dice(6),
      cmd.new Dice(6),
      // d6
      cmd.new Dice(6),
      // 1d
      cmd.new Dice(6),
      // d
      cmd.new Dice(6)
    };

    assertArrayEquals("parsing MdN formats works", expected, dice_array);
   }

  /**
   * Test of handle method, of class DiceCommand.
   */
  @Test
  public void testHandle() {
    // We have to adjust rolls because bounds are zero-inclusive, so in this case returning 2 gives
    // dice rolls of 3.
    SecureRandom rand = Mockito.mock(SecureRandom.class);
    Mockito.when(rand.nextInt(Mockito.isA(Integer.class))).thenReturn(2);

    // Build the command with the mocked SecureRandom
    DiceCommand cmd = new DiceCommand(rand);

    String message = "!roll d 1d6 1d20";
    String[] expResult = new String[]{"rolled 3 dice: d6=3 d6=3 d20=3"};
    String[] result = cmd.handle(message, "");
    assertArrayEquals("end-to-end command execution", expResult, result);
  }

}
