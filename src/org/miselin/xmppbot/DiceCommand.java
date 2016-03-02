/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.miselin.xmppbot;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rolls dice and sends the results.
 *
 * @author Matthew Iselin <matthew@theiselins.net>
 */
public class DiceCommand implements BaseCommand {

  private static SecureRandom random_ = new SecureRandom();
  private static final Pattern mdn_re_ = Pattern.compile("(?<count>\\d+)??d(?<sides>\\d+)??");

  @Override
  public String usage() {
    return "[MdN]+";
  }

  @Override
  public String description() {
    return "roll M N-sided dice; you may specify multiple MdNs; M and S are optional (default is 6 sides).";
  }

  public class Dice {

    private final int sides_;

    public Dice(int sides) {
      sides_ = sides;
    }

    public int getSides() {
      return sides_;
    }

    public int roll() {
      // nextInt is 0-bound inclusive, so add one to get a sane roll.
      return random_.nextInt(sides_ - 1) + 1;
    }

    @Override
    public boolean equals(Object other) {
      return other instanceof Dice && sides_ == ((Dice) other).sides_;
    }

    @Override
    public int hashCode() {
      // Netbeans generated this for me, and it seems like a reasonable hash.
      int hash = 5;
      hash = 79 * hash + this.sides_;
      return hash;
    }
  }

  public DiceCommand() {
    // Default random_ is OK.
  }

  public DiceCommand(SecureRandom override) {
    super();
    random_ = override;
  }

  @Override
  public String token() {
    return "roll";
  }

  public List<Dice> parseDice(String message) {
    List<Dice> dice = new ArrayList<>();
    for (String entry : message.split(" ")) {
      if (entry.startsWith("!")) {
        continue;
      }

      Matcher m = mdn_re_.matcher(entry);
      if (!m.matches()) {
        continue;
      }

      String scount = m.group("count");
      String ssides = m.group("sides");

      int count = 1;
      int sides = 6;

      if (null != scount) {
        count = Integer.parseInt(scount);
      }
      if (null != ssides) {
        sides = Integer.parseInt(ssides);
      }

      for (int i = 0; i < count; i++) {
        dice.add(new Dice(sides));
      }
    }

    if (dice.isEmpty()) {
      // Default to 6-sided die.
      dice.add(new Dice(6));
    }

    return dice;
  }

  @Override
  public String[] handle(String message, String from) {
    List<Dice> dice = parseDice(message);

    String response = String.format("rolled %d dice:", dice.size());
    for (Dice die : dice) {
      response += String.format(" d%d=%d", die.getSides(), die.roll());
    }

    return new String[]{response};
  }

}
