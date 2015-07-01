package im.tox.tox4j.exceptions

import org.jetbrains.annotations.NotNull

abstract class ToxException[E <: Enum[E]](errorCode: E, message: String) extends Exception(message) {

  @NotNull
  final override def getMessage: String = {
    message match {
      case "" => "Error code: " + errorCode.name
      case _  => message + ", error code: " + errorCode.name
    }
  }

  final def code: E = errorCode

}
