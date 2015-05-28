package net.sandrogrzicic.scalabuff.compiler

/**
 * Thrown when an input .proto file cannot be parsed successfully by the Parser.
 */
class ParsingFailureException(message: String) extends RuntimeException(message)
