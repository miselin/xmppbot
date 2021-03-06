
package org.miselin.xmppbot;

/**
 * BaseCommand defines the standard interface for all commands that the bot can action.
 *
 * @author Matthew Iselin <matthew@theiselins.net>
 */
public interface BaseCommand {

  /**
   * Get the token that will be used to match messages to this command.
   *
   * @return A String to match against; the "!" is not necessary in this function.
   */
  public abstract String token();

  /**
   * Get usage information (don't include the !token, only the parameters).
   *
   * @return Usage information.
   */
  public abstract String usage();

  /**
   * Get a short description of this command.
   *
   * @return Short description.
   */
  public abstract String description();


  /**
   * Handle the given message, returning any messages to transmit back to the user.
   *
   * @param message The message to handle, including the command token itself.
   * @param from The user that sent the message.
   * @return An array of String containing the messages to send as replies.
   */
  public abstract String[] handle(String message, String from);

}
