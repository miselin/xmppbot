/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.miselin.xmppbot;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Rolls dice and sends the results.
 *
 * @author Matthew Iselin <matthew@theiselins.net>
 */
public class DiceCommand implements BaseCommand {

    private static SecureRandom random_ = new SecureRandom();
    private static final Pattern mdn_re_ = Pattern.compile("(?<count>\\d+)??d(?<sides>\\d+)??(k(?<keep>\\d+)??)?");

    @Override
    public String usage() {
        return "[MdN(kO)]+";
    }

    @Override
    public String description() {
        return "roll M N-sided dice; you may specify multiple MdNs; M and S are optional (default is 6 sides). Optionally specify kO to keep the highest O dice.";
    }

    public class DiceSet {

        public final List<Dice> dice;
        public final int keep;

        public DiceSet(List<Dice> dicelist, int keepn) {
            dice = dicelist;
            if (keepn == -1) {
                keepn = dicelist.size();
            }
            keep = keepn;
        }

        public DiceSet(Dice[] dicelist, int keepn) {
            dice = Arrays.asList(dicelist);
            if (keepn == -1) {
                keepn = dicelist.length;
            }
            keep = keepn;
        }

        public DiceSet(Dice die) {
            dice = new ArrayList<>();
            dice.add(die);
            keep = 1;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof DiceSet)) {
                return false;
            }

            DiceSet ds = (DiceSet) obj;
            if (keep != ds.keep) {
                return false;
            }

            if (dice.size() != ds.dice.size()) {
                return false;
            }

            for (int i = 0; i < dice.size(); i++) {
                if (!(dice.get(i).equals(ds.dice.get(i)))) {
                    return false;
                }
            }

            return true;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            for (Dice die : this.dice) {
                hash = 71 * hash + die.hashCode();
            }
            hash = 71 * hash + this.keep;
            return hash;
        }
    }

    public class Roll {

        public final Dice die;
        public final int roll;

        public Roll(Dice die, int roll) {
            this.die = die;
            this.roll = roll;
        }
    }

    class RollComparator implements Comparator<Roll> {

        public int compare(Roll s1, Roll s2) {
            return Integer.compare(s1.roll, s2.roll);
        }
    }

    public class Dice {

        private final int sides_;

        public Dice(int sides) {
            sides_ = sides;
        }

        public int getSides() {
            return sides_;
        }

        public Roll roll() {
            // nextInt is 0-bound inclusive, so add one to get a sane roll.
            return new Roll(this, random_.nextInt(sides_ - 1) + 1);
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

    public List<DiceSet> parseDice(String message) {
        List<DiceSet> diceset = new ArrayList<>();
        for (String entry : message.split(" ")) {
            List<Dice> dice = new ArrayList<>();
            if (entry.startsWith("!")) {
                continue;
            }

            Matcher m = mdn_re_.matcher(entry);
            if (!m.matches()) {
                continue;
            }

            String scount = m.group("count");
            String ssides = m.group("sides");
            String skeep = m.group("keep");

            int count = 1;
            int sides = 6;
            int keep = -1;

            if (null != scount) {
                count = Integer.parseInt(scount);
            }
            if (null != ssides) {
                sides = Integer.parseInt(ssides);
            }
            if (null != skeep) {
                keep = Integer.parseInt(skeep);
            }

            for (int i = 0; i < count; i++) {
                dice.add(new Dice(sides));
            }

            diceset.add(new DiceSet(dice, keep));
        }

        if (diceset.isEmpty()) {
            // Default to 6-sided die.
            diceset.add(new DiceSet(new Dice(6)));
        }

        return diceset;
    }

    @Override
    public String[] handle(String message, String from) {
        List<DiceSet> dice = parseDice(message);

        int i = 1;

        String response = String.format("rolled %d dice sets:", dice.size());
        for (DiceSet diceset : dice) {
            response += String.format("\nset #%d keep %d:", i, diceset.keep);
            List<Roll> rolls = new ArrayList<>();
            List<Roll> discarded = new ArrayList<>();
            for (Dice die : diceset.dice) {
                rolls.add(die.roll());
            }

            // # of dice to keep
            Collections.sort(rolls, new RollComparator());
            for (int n = 0; n < (diceset.dice.size() - diceset.keep); n++) {
                discarded.add(rolls.get(0));
                rolls.remove(0);
            }

            for (Roll roll : rolls) {
                response += String.format(" d%d=%d", roll.die.getSides(), roll.roll);
            }

            if (discarded.size() > 0) {
                response += " discarded";
                for (Roll roll : discarded) {
                    response += String.format(" d%d=%d", roll.die.getSides(), roll.roll);
                }
            }

            i++;
        }

        return new String[]{response};
    }

}
