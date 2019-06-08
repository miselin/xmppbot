/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.miselin.xmppbot;

import org.miselin.xmppbot.DiceCommand;
import java.util.List;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.*;
import org.miselin.xmppbot.DiceCommand.Dice;
import org.miselin.xmppbot.DiceCommand.DiceSet;

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
        List<DiceSet> dice = cmd.parseDice("1d20 2d10 3d6 d6 1d d 4d6k3");
        DiceSet[] dice_array = dice.toArray(new DiceSet[dice.size()]);

        DiceSet[] expected = new DiceSet[]{
            // 1d20
            cmd.new DiceSet(cmd.new Dice(20)),
            // 2d10
            cmd.new DiceSet(new Dice[]{cmd.new Dice(10), cmd.new Dice(10)}, -1),
            // 3d6
            cmd.new DiceSet(new Dice[]{cmd.new Dice(6), cmd.new Dice(6), cmd.new Dice(6)}, -1),
            // d6
            cmd.new DiceSet(cmd.new Dice(6)),
            // 1d
            cmd.new DiceSet(cmd.new Dice(6)),
            // d
            cmd.new DiceSet(cmd.new Dice(6)),
            // 4d6k3
            cmd.new DiceSet(new Dice[]{cmd.new Dice(6), cmd.new Dice(6), cmd.new Dice(6), cmd.new Dice(6)}, 3)
        };

        assertEquals("correct number of dice sets parsed out", dice.size(), 7);
        assertArrayEquals("parsing MdN formats works", expected, dice_array);
    }

    private class FakeRandom extends Random {

        @Override
        public int nextInt() {
            return 2;
        }

        @Override
        public int nextInt(int bound) {
            return 2;
        }
    }

    /**
     * Test of handle method, of class DiceCommand.
     */
    @Test
    public void testHandle() {
        // We have to adjust rolls because bounds are zero-inclusive, so in this case returning 2 gives
        // dice rolls of 3.
        DiceCommand cmd = new DiceCommand(new FakeRandom());

        String message = "!roll d 1d6 1d20";
        String[] expResult = new String[]{"rolled 3 dice sets:\nset #1 keep 1: d6=3\nset #2 keep 1: d6=3\nset #3 keep 1: d20=3"};
        String[] result = cmd.handle(message, "");
        assertArrayEquals("end-to-end command execution", expResult, result);
    }

}
