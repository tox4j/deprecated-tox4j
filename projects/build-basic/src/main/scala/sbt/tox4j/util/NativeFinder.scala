/**
 * Derived from https://github.com/pfn/android-sdk-plugin/blob/master/src/NativeFinder.scala.
 */
package sbt.tox4j.util

import java.io.{ ByteArrayOutputStream, FileInputStream }
import java.lang.reflect.Method
import javassist.util.proxy.{ MethodFilter, MethodHandler, ProxyFactory }

import org.objectweb.asm._
import sbt._

// scalastyle:off
object NativeFinder {
  def natives(classes: Set[File]): Map[String, Seq[String]] = {
    var current: String = null
    var nativeList: Map[String, Seq[String]] = Map.empty

    val factory = new ProxyFactory()
    factory.setSuperclass(classOf[ClassVisitor])
    factory.setFilter(new MethodFilter {
      override def isHandled(p1: Method): Boolean = Seq("visit", "visitMethod").contains(p1.getName)
    })

    val h = new MethodHandler {
      override def invoke(self: scala.Any, thisMethod: Method, proceed: Method, args: Array[AnyRef]) = {
        thisMethod.getName match {
          case "visit" =>
            if (args.length > 2) {
              current = args(2).toString
            }
          case "visitMethod" =>
            val access = args(0).asInstanceOf[Int]
            val name = args(1).toString
            if ((access & Opcodes.ACC_NATIVE) != 0) {
              val className = current.replaceAll("/", ".")
              nativeList.get(className) match {
                case None =>
                  nativeList += ((className, Seq(name)))
                case Some(names) =>
                  nativeList += ((className, name +: names))
              }

            }
          case _ =>
        }
        null
      }
    }

    factory.create(Array(classOf[Int]), Array(Opcodes.ASM4.asInstanceOf[AnyRef]), h) match {
      case x: ClassVisitor =>
        val readbuf = Array.ofDim[Byte](Short.MaxValue)
        val buf = new ByteArrayOutputStream

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
