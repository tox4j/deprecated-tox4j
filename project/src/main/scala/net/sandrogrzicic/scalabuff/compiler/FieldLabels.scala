package net.sandrogrzicic.scalabuff.compiler

/**
 * Field labels.
 */
object FieldLabels {
  sealed trait EnumVal { def name: String }

  case object REQUIRED extends EnumVal { val name = "required" }
  case object OPTIONAL extends EnumVal { val name = "optional" }
  case object REPEATED extends EnumVal { val name = "repeated" }

  def apply(label: String) = {
    label match {
      case REQUIRED.name => REQUIRED
      case OPTIONAL.name => OPTIONAL
      case REPEATED.name => REPEATED
      case _             => throw new InvalidFieldLabelException(label)
    }
  }

  class InvalidFieldLabelException(label: String) extends RuntimeException(
    "Invalid field label: " + label
  )
}
