package im.tox.tox4j

import org.scalatest.FunSuite

import scalaz.Scalaz._

final class OptimisedTest extends FunSuite {

  @Optimised
  private final class Foo {
    def a[A <: Int](int: A): Int = int + 1
    def b(int: Int): Int = int + 2
    def c(int: Int): Int = int + 3

    def test(): Unit = {
      val piped = 1 |> a |> b |> c
      val called = c(b(a[Int](1)))
      assert(piped == called)
    }
  }

  test("|>") {
    new Foo().test()
  }

}
