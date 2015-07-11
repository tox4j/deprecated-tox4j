package im.tox.tox4j.impl.jni.codegen

import java.io.{ File, PrintWriter }

import com.google.common.base.CaseFormat
import im.tox.tox4j.av.enums.{ ToxavCallControl, ToxavCallState }
import im.tox.tox4j.core.enums._

object JniEnums extends App {

  def withFile(path: File)(printCode: PrintWriter => Unit): Unit = {
    val writer = new PrintWriter(path)
    try {
      printCode(writer)
    } finally {
      writer.close()
    }
  }

  private def generateEnumConversions[E <: Enum[E]](out: PrintWriter, values: Array[E]): Unit = {
    val javaEnum = values(0).getClass.getSimpleName
    val cEnum = CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, javaEnum)

    out.println()
    out.println("template<>")
    out.println("jint")
    out.println(s"enum_ordinal<$cEnum> (JNIEnv *env, $cEnum value)")
    out.println("{")
    out.println("  switch (value)")
    out.println("    {")
    values foreach { value =>
      out.println(s"    case ${cEnum}_${value.name}: return ${value.ordinal()};")
    }
    out.println("    }")
    out.println("  tox4j_fatal (\"Invalid enumerator from toxcore\");")
    out.println("}")

    out.println("template<>")
    out.println(cEnum)
    out.println(s"enum_value<$cEnum> (JNIEnv *env, jint ordinal)")
    out.println("{")
    out.println("  switch (ordinal)")
    out.println("    {")
    values foreach { value =>
      out.println(s"    case ${value.ordinal()}: return ${cEnum}_${value.name};")
    }
    out.println("    }")
    out.println("  tox4j_fatal (\"Invalid enumerator from Java\");")
    out.println("}")

    out.println("template<>")
    out.println("void")
    out.println(s"print_arg<$cEnum> ($cEnum value)")
    out.println("{")
    out.println("  switch (value)")
    out.println("    {")
    values foreach { value =>
      val enum = cEnum + "_" + value.name
      out.println("    case " + enum + ": debug_out << \"" + enum + "\"; break;")
    }
    out.println("    default: debug_out << \"(" + cEnum + ")\" << value; break;")
    out.println("    }")
    out.println("}")
  }

  withFile(new File("src/main/cpp/ToxAv/enums.cpp")) { out =>
    out.println("#include \"ToxAv.h\"")
    out.println("#ifdef TOXAV_VERSION_MAJOR")
    generateEnumConversions(out, ToxavCallControl.values)
    generateEnumConversions(out, ToxavCallState.values)
    out.println("#endif")
  }

  withFile(new File("src/main/cpp/ToxCore/enums.cpp")) { out =>
    out.println("#include \"ToxCore.h\"")
    out.println("#ifdef TOX_VERSION_MAJOR")
    generateEnumConversions(out, ToxConnection.values)
    generateEnumConversions(out, ToxFileControl.values)
    generateEnumConversions(out, ToxMessageType.values)
    generateEnumConversions(out, ToxProxyType.values)
    generateEnumConversions(out, ToxSavedataType.values)
    generateEnumConversions(out, ToxUserStatus.values)
    out.println("#endif")
  }

}
