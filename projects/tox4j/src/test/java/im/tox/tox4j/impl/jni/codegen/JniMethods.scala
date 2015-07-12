package im.tox.tox4j.impl.jni.codegen

import java.lang.reflect.Modifier

import im.tox.tox4j.impl.jni.codegen.cxx.Ast._
import im.tox.tox4j.impl.jni.{ ToxAvJni, ToxCoreJni }

object JniMethods extends CodeGenerator {

  def generateNativeDecls(clazz: Class[_]): TranslationUnit = {
    clazz.getDeclaredMethods
      .filter { method =>
        Modifier.isNative(method.getModifiers) &&
          method.getName.startsWith("tox")
      }
      .sortBy(method => method.getName)
      .flatMap { method =>
        Seq(
          MacroCall(FunCall(Identifier("JAVA_METHOD_REF"), Seq(Identifier(method.getName)))),
          MacroCall(FunCall(Identifier("CXX_FUNCTION_REF"), Seq(Identifier(cxxVarName(method.getName)))))
        )
      }
  }

  writeCode("ToxAv/generated/natives.h", "\n") {
    Comment(classOf[ToxAvJni].getName) +:
      generateNativeDecls(classOf[ToxAvJni])
  }

  writeCode("ToxCore/generated/natives.h", "\n") {
    Comment(classOf[ToxCoreJni].getName) +:
      generateNativeDecls(classOf[ToxCoreJni])
  }

}
