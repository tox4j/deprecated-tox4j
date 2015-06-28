package im.tox.tox4j.lint

import org.scalatest.FunSuite

final class OptionsClassesTest extends FunSuite {

  test("non-options classes are not flagged") {
    val result = WartRemoverTest(OptionsClasses) {
      class Foo(thing: Any)
    }

    assert(result.errors == Nil)
  }

}
