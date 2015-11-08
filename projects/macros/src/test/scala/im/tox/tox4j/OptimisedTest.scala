package im.tox.tox4j

import org.scalatest.FunSuite

import scalaz.Scalaz._

final class OptimisedTest extends FunSuite {

  test("|>") {
    @Optimised
    final class Foo {
      def a(int: Int): Int = int + 1
      def b(int: Int): Int = int + 2
      def c(int: Int): Int = int + 3

      def test(): Unit = {
        assert((1 |> a |> b |> c) == c(b(a(1))))
      }
    }
  }

}
