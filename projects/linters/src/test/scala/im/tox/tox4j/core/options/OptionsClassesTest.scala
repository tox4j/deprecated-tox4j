package im.tox.tox4j.core.options

import im.tox.tox4j.lint.{OptionsClasses, WartRemoverTest}
import org.scalatest.FunSuite

import scala.collection.mutable.ArrayBuffer

final class OptionsClassesTest extends FunSuite {

  test("options classes are flagged") {
    val result = WartRemoverTest(OptionsClasses) {
      class Foo(thing: Any)
    }

    assert(result.errors == List(OptionsClasses.errorCaseClass))
  }

  test("options traits are not flagged") {
    val result = WartRemoverTest(OptionsClasses) {
      trait Foo
    }

    assert(result.errors == Nil)
  }

  test("options classes must have default arguments") {
    val result = WartRemoverTest(OptionsClasses) {
      case class Foo(thing: Any)
    }

    assert(result.errors == List(OptionsClasses.errorDefaultArguments))
  }

  test("case classes with base trait in options package don't need default arguments") {
    val result = WartRemoverTest(OptionsClasses) {
      trait Base
      case class Foo(thing: Any) extends Base
    }

    assert(result.errors == Nil)
  }

  test("inheritance from anything outside the options package is not allowed") {
    val result = WartRemoverTest(OptionsClasses) {
      case class Foo(thing: Any) extends ArrayBuffer[Int]
    }

    assert(result.errors == List(
      OptionsClasses.errorBadParent(classOf[ArrayBuffer[_]].getName),
      OptionsClasses.errorDefaultArguments
    ))
  }

  test("options classes with default arguments are not flagged") {
    val result = WartRemoverTest(OptionsClasses) {
      case class Foo(thing: Any = 0)
    }

    assert(result.errors == Nil)
  }

  test("non-case-classes without arguments are flagged") {
    val result = WartRemoverTest(OptionsClasses) {
      class Foo
    }

    assert(result.errors == List(OptionsClasses.errorCaseClass))
  }

  test("options case objects are fine") {
    val result = WartRemoverTest(OptionsClasses) {
      case object Foo
    }

    assert(result.errors == Nil)
  }

}
