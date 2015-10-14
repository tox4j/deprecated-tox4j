package im.tox.tox4j.core.options

import im.tox.tox4j.lint.ToxOptionsClasses
import org.brianmckenna.wartremover.test.WartTestTraverser
import org.scalatest.FunSuite

import scala.collection.mutable.ArrayBuffer

/**
 * This class is in this particular package, because the OptionsClasses test is
 * very specific to the classes in the options packages in tox4j.
 */
final class ToxOptionsClassesTest extends FunSuite {

  test("options classes are flagged") {
    val result = WartTestTraverser(ToxOptionsClasses) {
      class Foo(thing: Any)
    }

    assert(result.errors == List(ToxOptionsClasses.errorCaseClass))
  }

  test("options traits are not flagged") {
    val result = WartTestTraverser(ToxOptionsClasses) {
      trait Foo
    }

    assert(result.errors == Nil)
  }

  test("options classes must have default arguments") {
    val result = WartTestTraverser(ToxOptionsClasses) {
      case class Foo(thing: Any)
    }

    assert(result.errors == List(ToxOptionsClasses.errorDefaultArguments))
  }

  test("case classes with base trait in options package don't need default arguments") {
    val result = WartTestTraverser(ToxOptionsClasses) {
      trait Base
      case class Foo(thing: Any) extends Base
    }

    assert(result.errors == Nil)
  }

  test("inheritance from anything outside the options package is not allowed") {
    val result = WartTestTraverser(ToxOptionsClasses) {
      case class Foo(thing: Any) extends ArrayBuffer[Int]
    }

    assert(result.errors == List(
      ToxOptionsClasses.errorBadParent(classOf[ArrayBuffer[_]].getName),
      ToxOptionsClasses.errorDefaultArguments
    ))
  }

  test("options classes with default arguments are not flagged") {
    val result = WartTestTraverser(ToxOptionsClasses) {
      case class Foo(thing: Any = 0)
    }

    assert(result.errors == Nil)
  }

  test("non-case-classes without arguments are flagged") {
    val result = WartTestTraverser(ToxOptionsClasses) {
      class Foo
    }

    assert(result.errors == List(ToxOptionsClasses.errorCaseClass))
  }

  test("options case objects are fine") {
    val result = WartTestTraverser(ToxOptionsClasses) {
      case object Foo
    }

    assert(result.errors == Nil)
  }

}
