/*
 * The MIT License
 *
 * Copyright 2016 Matthew Iselin <matthew@theiselins.net>.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package org.miselin.xmppbot.util;

/**
 * Provides a set of custom utilities to do with strings.
 *
 * @author Matthew Iselin <matthew@theiselins.net>
 */
public class StringUtils {

  /**
   * Return the correct arrow for the difference between @a and @b.
   *
   * @param <T> type for @a and @b.
   * @param a the first item to check
   * @param b the second item to check
   * @return a string representing visually the difference between @a and @b.
   */
  public static <T extends Comparable> String arrow(T a, T b) {
    if (a.compareTo(b) < 0) {
      return "↑";
    } else if (a.compareTo(b) > 0) {
      return "↓";
    } else {
      return "=";
    }
  }

}
