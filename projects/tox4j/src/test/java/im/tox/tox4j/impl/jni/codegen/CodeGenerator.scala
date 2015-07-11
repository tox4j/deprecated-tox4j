package im.tox.tox4j.impl.jni.codegen

import java.io.{ File, PrintWriter }

import com.google.common.base.CaseFormat

abstract class CodeGenerator extends App {

  def writeFile(path: File)(code: => String): Unit = {
    val writer = new PrintWriter(path)
    try {
      writer.println(code.trim)
    } finally {
      writer.close()
    }
  }

  def cxxVarName(name: String): String = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name)
  def cxxTypeName(name: String): String = CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, name)
  def javaTypeName(name: String): String = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name)

  def mkLines[T](strings: Seq[T])(transform: T => String): String = {
    strings.map(transform).mkString("\n")
  }

}
