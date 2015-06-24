package org.scalastyle.scalariform.tox4j

// scalastyle:off magic.number
final class ForBraceCheckerTest extends CheckerTest(new ForBraceChecker) {

  "for.brace" should "flag braces on the same line" in {
    val source = """
class Foo {
  for { t <- List(1,2,3) } yield t
}
                 """

    assertErrors(List(columnError(3, 6)), source)
  }

  it should "not flag parentheses on the same line" in {
    val source = """
class Foo {
  for (t <- List(1,2,3)) yield t
}
                 """

    assertErrors(Nil, source)
  }

  it should "flag parentheses on a separate line" in {
    val source = """
class Foo {
  for (
    t <- List(1,2,3)
  ) yield t
}
                 """

    assertErrors(List(columnError(3, 6)), source)
  }

  it should "not flag braces on a separate line" in {
    val source = """
class Foo {
  for {
    t <- List(1,2,3)
  } yield t
}
                 """

    assertErrors(Nil, source)
  }

}
