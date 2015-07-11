package im.tox.tox4j.impl.jni.codegen

import com.google.common.base.CaseFormat
import im.tox.tox4j.av.enums.{ ToxavCallControl, ToxavFriendCallState }
import im.tox.tox4j.core.enums._

object JniEnums extends CodeGenerator {

  def generateEnumConversions[E <: Enum[E]](values: Array[E]): String = {
    val javaEnum = values(0).getClass.getSimpleName
    val cEnum = CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, javaEnum)
    def cEnumerator(value: E) = cEnum + "_" + value.name

    s"""
    |template<>
    |jint
    |enum_ordinal<$cEnum> (JNIEnv *env, $cEnum value)
    |{
    |  switch (value)
    |    {
    |${mkLines(values) { value => s"    case ${cEnumerator(value)}: return ${value.ordinal};" }}
    |    }
    |  tox4j_fatal ("Invalid enumerator from toxcore");
    |}
    |
    |template<>
    |$cEnum
    |enum_value<$cEnum> (JNIEnv *env, jint ordinal)
    |{
    |  switch (ordinal)
    |    {
    |${mkLines(values) { value => s"    case ${value.ordinal}: return ${cEnumerator(value)};" }}
    |    }
    |  tox4j_fatal ("Invalid enumerator from Java");
    |}
    |
    |template<>
    |void
    |print_arg<$cEnum> ($cEnum value)
    |{
    |  switch (value)
    |    {
    |${mkLines(values) { value => "    case " + cEnumerator(value) + ": debug_out << \"" + cEnumerator(value) + "\"; break;" }}
    |    default: debug_out << "($cEnum)" << value; break;
    |    }
    |}
    |""".stripMargin
  }

  writeFile("ToxAv/generated/enums.cpp") {
    s"""
    |#include "../ToxAv.h"
    |
    |#ifdef TOXAV_VERSION_MAJOR
    |
    |${generateEnumConversions(ToxavCallControl.values)}
    |${generateEnumConversions(ToxavFriendCallState.values)}
    |
    |#endif
    |""".stripMargin
  }

  writeFile("ToxCore/generated/enums.cpp") {
    s"""
    |#include "../ToxCore.h"
    |
    |#ifdef TOX_VERSION_MAJOR
    |
    |${generateEnumConversions(ToxConnection.values)}
    |${generateEnumConversions(ToxFileControl.values)}
    |${generateEnumConversions(ToxMessageType.values)}
    |${generateEnumConversions(ToxProxyType.values)}
    |${generateEnumConversions(ToxSavedataType.values)}
    |${generateEnumConversions(ToxUserStatus.values)}
    |
    |#endif
    |""".stripMargin
  }

}
