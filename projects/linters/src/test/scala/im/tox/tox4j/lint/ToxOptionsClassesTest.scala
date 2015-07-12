package im.tox.tox4j.lint

import org.brianmckenna.wartremover.test.WartTestTraverser
import org.scalatest.FunSuite

final class ToxOptionsClassesTest extends FunSuite {

  test("non-options classes are not flagged") {
    val result = WartTestTraverser(ToxOptionsClasses) {
      class Foo(thing: Any)
    }

    assert(result.errors == Nil)
  }

}
