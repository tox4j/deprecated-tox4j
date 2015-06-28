package im.tox.tox4j.lint

import org.scalatest.FunSuite

final class EqualsNoneTest extends FunSuite {

  test("x == None is flagged") {
    val result = WartRemoverTest(EqualsNone) {
      def x: Option[Int] = Some(1)
      // noinspection EmptyCheck
      x == None
    }

    assert(result.errors == List(EqualsNone.errorMessage))
  }

  test("None == x is flagged") {
    val result = WartRemoverTest(EqualsNone) {
      def x: Option[Int] = Some(1)
      // noinspection EmptyCheck
      None == x
    }

    assert(result.errors == List(EqualsNone.errorMessage))
  }

  test("x == y where y: None is flagged") {
    val result = WartRemoverTest(EqualsNone) {
      def x: Option[Int] = Some(1)
      def y: None.type = None
      x == y
    }

    assert(result.errors == List(EqualsNone.errorMessage))
  }

  test("x != None is flagged") {
    val result = WartRemoverTest(EqualsNone) {
      def x: Option[Int] = Some(1)
      // noinspection EmptyCheck
      x != None
    }

    assert(result.errors == List(EqualsNone.errorMessage))
  }

  test("None != x is flagged") {
    val result = WartRemoverTest(EqualsNone) {
      def x: Option[Int] = Some(1)
      // noinspection EmptyCheck
      None != x
    }

    assert(result.errors == List(EqualsNone.errorMessage))
  }

  test("None eq x is not flagged") {
    val result = WartRemoverTest(EqualsNone) {
      def x: Option[Int] = Some(1)
      None eq x
    }

    assert(result.errors == Nil)
  }

  test("None == x for a non-scala.Option 'None' is not flagged") {
    val result = WartRemoverTest(EqualsNone) {
      // Define our own Option that should not be flagged.
      sealed trait Option[+A]
      case object None extends Option[Nothing]
      final case class Some[+A](a: A) extends Option[A]

      def x: Option[Int] = Some(1)
      None == x
    }

    assert(result.errors == Nil)
  }

  test("x == None in a match guard is not allowed") {
    val result = WartRemoverTest(EqualsNone) {
      def x: Option[Int] = Some(1)
      x match {
        // noinspection EmptyCheck
        case maybe if maybe == None =>
        case _                      =>
      }
    }

    assert(result.errors == List(EqualsNone.errorMessage))
  }

  test("both guard and body of a match case are checked") {
    val result = WartRemoverTest(EqualsNone) {
      def x: Option[Int] = Some(1)
      x match {
        // noinspection EmptyCheck
        case maybe if maybe == None =>
          None == maybe
        case _ =>
      }
    }

    assert(result.errors == List(EqualsNone.errorMessage, EqualsNone.errorMessage))
  }

  test("comparing for None in match-statements is allowed") {
    val result = WartRemoverTest(EqualsNone) {
      sys.env.get("HOME") match {
        case None    =>
        case Some(_) =>
      }
    }

    assert(result.errors == Nil)
  }

}
