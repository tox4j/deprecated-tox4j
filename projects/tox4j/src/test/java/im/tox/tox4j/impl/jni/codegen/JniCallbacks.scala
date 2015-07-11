package im.tox.tox4j.impl.jni.codegen

import java.io.File

import im.tox.tox4j.av.callbacks.ToxAvEventAdapter
import im.tox.tox4j.core.callbacks.ToxEventAdapter

object JniCallbacks extends CodeGenerator {

  def generateCallbacks(clazz: Class[_]): String = {
    clazz.getMethods.sortBy(method => method.getName).flatMap { method =>
      if (method.getDeclaringClass == clazz) {
        Seq("CALLBACK (" + cxxVarName(method.getName) + ")")
      } else {
        Nil
      }
    }.mkString("\n")
  }

  writeFile(new File("src/main/cpp/tox/generated/av.h")) {
    generateCallbacks(classOf[ToxAvEventAdapter[_]])
  }

  writeFile(new File("src/main/cpp/tox/generated/core.h")) {
    generateCallbacks(classOf[ToxEventAdapter[_]])
  }

}
