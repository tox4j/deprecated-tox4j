package im.tox.tox4j.lint

import org.brianmckenna.wartremover.test.WartTestTraverser
import org.scalatest.FunSuite

final class OverrideTest extends FunSuite {

  private val fooNeedsOverride = List(Override.errorMessage("foo"))

  test("flag methods overriding abstract methods without 'override' modifier") {
    val result = WartTestTraverser(Override) {
      abstract class Base {
        def foo(): Unit
      }
      class Derived extends Base {
        def foo(): Unit = ()
      }
    }

    assert(result.errors == fooNeedsOverride)
  }

  test("flag methods whose base is not in a direct superclass") {
    val result = WartTestTraverser(Override) {
      abstract class Base {
        def foo(): Unit
      }
      abstract class Intermediate extends Base
      class Derived extends Intermediate {
        def foo(): Unit = ()
      }
    }

    assert(result.errors == fooNeedsOverride)
  }

  test("flag methods in a base trait") {
    val result = WartTestTraverser(Override) {
      trait Base {
        def foo(): Unit
      }
      abstract class Intermediate extends Base
      class Derived extends Intermediate {
        def foo(): Unit = ()
      }
    }

    assert(result.errors == fooNeedsOverride)
  }

  test("don't flag methods with a correct 'override' modifier") {
    val result = WartTestTraverser(Override) {
      abstract class Base {
        def foo(): Int
      }
      class Derived extends Base {
        override def foo(): Int = 2
      }
    }

    assert(result.errors == Nil)
  }

  test("don't flag synthesised methods (productArity et al.) and case class accessors") {
    val result = WartTestTraverser(Override) {
      abstract class Base {
        def foo: Int
      }
      case class Derived(foo: Int) extends Base
    }

    assert(result.errors == Nil)
  }

  test("warn about possibly overridden base overloads") {
    val result = WartTestTraverser(Override) {
      abstract class Base {
        def foo: Int
        def foo(i: Int): Unit = ()
      }
      class Derived extends Base {
        def foo: Int = 2
      }
    }

    assert(result.errors == Nil)
    assert(result.warnings == List(Override.warningOverloads("foo")))
  }

}
