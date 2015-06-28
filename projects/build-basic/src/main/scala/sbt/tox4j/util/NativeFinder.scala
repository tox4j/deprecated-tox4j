/**
 * Derived from https://github.com/pfn/android-sdk-plugin/blob/master/src/NativeFinder.scala.
 */
package sbt.tox4j.util

import java.io.FileInputStream
import java.lang.reflect.Method
import javassist.util.proxy.{ MethodFilter, MethodHandler, ProxyFactory }

import org.objectweb.asm._
import sbt._

object NativeFinder {
  def natives(classes: Set[File]): Map[String, Seq[String]] = {
    var currentClassName: String = null
    var nativeList: Map[String, Seq[String]] = Map.empty

    val factory = new ProxyFactory()
    factory.setSuperclass(classOf[ClassVisitor])
    factory.setFilter(new MethodFilter {
      override def isHandled(m: Method): Boolean = Seq("visit", "visitMethod").contains(m.getName)
    })

    val handler = new MethodHandler {
      override def invoke(self: scala.Any, thisMethod: Method, proceed: Method, args: Array[AnyRef]) = {
        thisMethod.getName match {
          case "visit" =>
            if (args.length > 2) {
              currentClassName = args(2).toString.replaceAll("/", ".")
            }
          case "visitMethod" =>
            val access = args(0).asInstanceOf[Int]
            val name = args(1).toString
            if ((access & Opcodes.ACC_NATIVE) != 0) {
              nativeList.get(currentClassName) match {
                case None =>
                  nativeList += ((currentClassName, Seq(name)))
                case Some(names) =>
                  nativeList += ((currentClassName, name +: names))
              }

            }
          case _ =>
        }
        null
      }
    }

    factory.create(Array(classOf[Int]), Array(Integer.valueOf(Opcodes.ASM4)), handler) match {
      case x: ClassVisitor =>
        classes foreach { entry =>
          val in = new FileInputStream(entry)
          val r = new ClassReader(in)
          r.accept(x, 0)
          in.close()
        }

        nativeList
    }
  }
}
