package im.tox.tox4j.lint

import org.brianmckenna.wartremover.test.WartTestTraverser
import org.scalatest.FunSuite

final class OptionsClassesTest extends FunSuite {

  test("non-options classes are not flagged") {
    val result = WartTestTraverser(OptionsClasses) {
      class Foo(thing: Any)
    }

    assert(result.errors == Nil)
  }

}
