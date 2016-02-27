/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.wikiforall.wfabot;

import java.security.SecureRandom;

/**
 * Rolls dice and sends the results.
 *
 * @author Matthew Iselin <matthew@theiselins.net>
 */
public class DiceCommand implements BaseCommand {

  private final SecureRandom random_ = new SecureRandom();

  public int roll(int sides) {
    // nextInt is 0-bound inclusive, so add one to get a sane roll.
    return random_.nextInt(sides - 1) + 1;
  }

  @Override
  public String token() {
    return "roll";
  }

  @Override
  public String[] handle(String message, String from) {
    return new String[]{String.format("rolled a %d", roll(6))};
  }

}
