package im.tox.tox4j.testing

import org.scalacheck.Arbitrary

import scala.collection.mutable
import scala.reflect.ClassTag

import scala.language.implicitConversions

final case class NonEmptyByteArray(array: Array[Byte]) extends mutable.WrappedArray[Byte] {
  require(array.nonEmpty)

  override def elemTag: ClassManifest[Byte] = ClassTag.Byte
  override def length: Int = array.length
  override def apply(index: Int): Byte = array(index)
  override def update(index: Int, elem: Byte): Unit = { array(index) = elem }
}

object NonEmptyByteArray {
  object Conversions {
    implicit def nonEmptyArrayToArray(nonEmptyByteArray: NonEmptyByteArray): Array[Byte] = nonEmptyByteArray.array

    implicit val arbNonEmptyByteArray: Arbitrary[NonEmptyByteArray] =
      Arbitrary(Arbitrary.arbitrary[Array[Byte]].filter(_.nonEmpty).map(NonEmptyByteArray(_)))
  }
}
