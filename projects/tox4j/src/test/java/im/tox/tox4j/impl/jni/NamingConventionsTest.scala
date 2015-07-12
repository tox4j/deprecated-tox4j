package im.tox.tox4j.impl.jni

import java.lang.reflect.Modifier

import im.tox.tox4j.impl.jni.codegen.NameConversions
import org.scalatest.FunSuite

import scalaz.Scalaz._

abstract class NamingConventionsTest(jniClass: Class[_], traitClass: Class[_]) extends FunSuite {

  val actions = Seq("get", "set", "add", "delete")
  val exemptions = Seq("callback", "load", "close", "create")

  def removeSelf(name: Seq[String]): Seq[String] = {
    name.headOption match {
      case Some("self") => name.tail
      case _            => name
    }
  }

  def moveActionToFront(name: Seq[String]): Seq[String] = {
    name.indexWhere(actions.contains) match {
      case -1 =>
        name
      case actionIndex =>
        name(actionIndex) +: (name.slice(0, actionIndex) ++ name.slice(actionIndex + 1, name.length))
    }
  }

  test("Java method names should be derivable from JNI method names") {
    val jniMethods =
      jniClass
        .getDeclaredMethods.toSeq
        .filter { method =>
          Modifier.isNative(method.getModifiers) &&
            method.getName.startsWith("tox")
        }
        .map { method =>
          (method.getName
            |> NameConversions.cxxVarName
            |> (_.split("_").toSeq.tail)
            |> removeSelf
            |> moveActionToFront
            |> (_.mkString("_"))
            |> NameConversions.javaVarName)
        }

    traitClass
      .getDeclaredMethods.toSeq
      .map(_.getName)
      .filterNot(exemptions.contains)
      .foreach { name =>
        assert(jniMethods.contains(name))
      }
  }

}
