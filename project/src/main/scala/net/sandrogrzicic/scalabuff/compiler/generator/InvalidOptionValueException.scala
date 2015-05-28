package net.sandrogrzicic.scalabuff.compiler.generator

/**
 * Created by pippijn on 28/05/15.
 */
class InvalidOptionValueException(key: String, value: String) extends GenerationFailureException(
  "Invalid option value " + value + " for key " + key
)
