package org.scalastyle.scalariform.tox4j

import org.scalastyle.scalariform.CheckerTest

// scalastyle:off magic.number
final class RedundantValCheckerTest extends CheckerTest(new RedundantValChecker) {

  key should "flag redundant val in case classes" in {
    assertErrors(1, "case class Foo(!!val thing: Int)")
  }

  it should "flag redundant val in final case classes" in {
    assertErrors(1, "final case class Foo(!!val thing: Int)")
  }

  it should "not flag var in case classes" in {
    assertErrors(0, "case class Foo(var thing: Int)")
  }

  it should "not flag case classes without val" in {
    assertErrors(0, "case class Foo(thing: Int)")
    assertErrors(0, "final case class Foo(thing: Int)")
    assertErrors(0, "abstract case class Foo(thing: Int)")
  }

  it should "not flag normal classes with val inside case classes" in {
    assertErrors(0, """
case class Foo(thing: Int) {
  class Bar(val otherThing: Int)
}""")
  }

  it should "not flag val in non-case classes" in {
    assertErrors(0, """
class Foo(val thing: Int)
abstract class Foo(val thing: Int)
final class Foo(val thing: Int)""")
  }

}
