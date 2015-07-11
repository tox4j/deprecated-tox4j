package im.tox.tox4j.impl.jni.codegen

import java.io.{ PrintWriter, File }

import im.tox.tox4j.av.callbacks.ToxAvEventAdapter
import im.tox.tox4j.core.callbacks.ToxEventAdapter

object JniCallbacks extends CodeGenerator {

  def printCallbacks(out: PrintWriter, clazz: Class[_]): Unit = {
    clazz.getMethods.sortBy(method => method.getName).foreach { method =>
      if (method.getDeclaringClass == clazz) {
        out.println("CALLBACK (" + cxxVarName(method.getName) + ")")
      }
    }
  }

  withFile(new File("src/main/cpp/tox/generated/av.h")) { out =>
    printCallbacks(out, classOf[ToxAvEventAdapter[_]])
  }

  withFile(new File("src/main/cpp/tox/generated/core.h")) { out =>
    printCallbacks(out, classOf[ToxEventAdapter[_]])
  }

}
