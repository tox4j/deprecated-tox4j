package net.sandrogrzicic.scalabuff.compiler

import com.google.protobuf.WireFormat._

import scala.language.implicitConversions

/**
 * Field types; both predefined and custom types are an instance of FieldTypes.EnumVal.
 */
object FieldTypes extends Enum {
  implicit def buffString(string: String): BuffedString = new BuffedString(string)

  /**
   * Represents a predefined field type.
   * @param name the field type name
   * @param scalaType the output field type name
   * @param defaultValue field type default value
   * @param wireType the field wire type.
   * @param isEnum whether the field is an Enum
   * @param isMessage whether the field is a Message
   */
  sealed trait EnumVal extends Value {
    var name: String
    var scalaType: String
    var defaultValue: String
    var valueMethod: String
    var wireType: Int
    var isEnum: Boolean = false
    var isMessage: Boolean = false

    def packable: Boolean = {
      !isMessage && (isEnum || wireType == WIRETYPE_VARINT || wireType == WIRETYPE_FIXED64 || wireType == WIRETYPE_FIXED32)
    }
  }

  /**
   * A predefined field type; immutable.
   */
  sealed class PredefinedEnumVal private[FieldTypes] (
      val name: String,
      val scalaType: String,
      val defaultValue: String,
      val wireType: Int
  ) extends EnumVal {
    def name_=(name: String): Unit = {}
    def scalaType_=(scalaType: String): Unit = {}
    def defaultValue_=(defaultValue: String): Unit = {}
    def valueMethod: String = ""
    def valueMethod_=(valueMethod: String): Unit = {}
    def wireType_=(wireType: Int): Unit = {}
  }

  case object INT32 extends PredefinedEnumVal("Int32", "Int", "0", WIRETYPE_VARINT)
  case object UINT32 extends PredefinedEnumVal("UInt32", "Int", "0", WIRETYPE_VARINT)
  case object SINT32 extends PredefinedEnumVal("SInt32", "Int", "0", WIRETYPE_VARINT)
  case object FIXED32 extends PredefinedEnumVal("Fixed32", "Int", "0", WIRETYPE_FIXED32)
  case object SFIXED32 extends PredefinedEnumVal("SFixed32", "Int", "0", WIRETYPE_FIXED32)
  case object INT64 extends PredefinedEnumVal("Int64", "Long", "0L", WIRETYPE_VARINT)
  case object UINT64 extends PredefinedEnumVal("UInt64", "Long", "0L", WIRETYPE_VARINT)
  case object SINT64 extends PredefinedEnumVal("SInt64", "Long", "0L", WIRETYPE_VARINT)
  case object FIXED64 extends PredefinedEnumVal("Fixed64", "Long", "0L", WIRETYPE_FIXED64)
  case object SFIXED64 extends PredefinedEnumVal("SFixed64", "Long", "0L", WIRETYPE_FIXED64)
  case object BOOL extends PredefinedEnumVal("Bool", "Boolean", "false", WIRETYPE_VARINT)
  case object FLOAT extends PredefinedEnumVal("Float", "Float", "0.0f", WIRETYPE_FIXED32)
  case object DOUBLE extends PredefinedEnumVal("Double", "Double", "0.0", WIRETYPE_FIXED64)
  case object BYTES extends PredefinedEnumVal("Bytes", "com.google.protobuf.ByteString", "com.google.protobuf.ByteString.EMPTY", WIRETYPE_LENGTH_DELIMITED)
  case object STRING extends PredefinedEnumVal("String", "String", "\"\"", WIRETYPE_LENGTH_DELIMITED)

  /**
   * A custom field type representing a Message or an Enum.
   */
  final case class CustomEnumVal private[FieldTypes] (
    var name: String,
    var scalaType: String,
    var defaultValue: String,
    var valueMethod: String,
    var wireType: Int
  ) extends EnumVal

  /**
   * Returns an immutable FieldType.PredefinedEnumVal based on the specified proto field type,
   * or a new EnumVal with a null default value if it's a custom Message or Enum type.
   */
  def apply(fieldType: String) = {
    fieldType match {
      case "int32"     => INT32
      case "uint32"    => UINT32
      case "sint32"    => SINT32
      case "fixed32"   => FIXED32
      case "sfixed32"  => SFIXED32
      case "int64"     => INT64
      case "uint64"    => UINT64
      case "sint64"    => SINT64
      case "fixed64"   => FIXED64
      case "sfixed64"  => SFIXED64
      case "bool"      => BOOL
      case "float"     => FLOAT
      case "double"    => DOUBLE
      case "bytes"     => BYTES
      case "string"    => STRING
      case `fieldType` => CustomEnumVal(fieldType, fieldType, "null", "", WIRETYPE_LENGTH_DELIMITED)
    }
  }

}
