
package org.miselin.xmppbot;

/**
 * BaseCommand defines the standard interface for all commands that the bot can
 * action.
 *
 * @author Matthew Iselin <matthew@theiselins.net>
 */
public interface BaseCommand {

  /**
   * Get the token that will be used to match messages to this command.
   * @return A String to match against; no need to add a "!".
   */
  String token();

  /**
   * Get usage information (don't include the !token, only the parameters).
   *
   * @return Usage information.
   */
  String usage();

  /**
   * Get a short description of this command.
   *
   * @return Short description.
   */
  String description();


  /**
   * Handle the given message, returning any messages to respond with.
   * @param message The message to handle, including the command token itself.
   * @param from The user that sent the message.
   * @return An array of String containing the messages to send as replies.
   */
  String[] handle(String message, String from);

}
