package im.tox.tox4j.testing

import org.scalacheck.Arbitrary

import scala.collection.mutable
import scala.language.implicitConversions
import scala.reflect.ClassTag

class WrappedByteArray(val array: Array[Byte]) extends mutable.WrappedArray[Byte] with Serializable {
  override def elemTag: ClassTag[Byte] = ClassTag.Byte
  override def length: Int = array.length
  override def apply(index: Int): Byte = array(index)
  override def update(index: Int, elem: Byte): Unit = { array(index) = elem }
}

final class NonEmptyByteArray(array: Array[Byte]) extends WrappedByteArray(array) {
  require(array.nonEmpty)
}

object WrappedByteArray {
  object Conversions {
    implicit def unwrapArray(wrappedArray: WrappedByteArray): Array[Byte] = wrappedArray.array

    implicit val arbNonEmptyByteArray: Arbitrary[NonEmptyByteArray] =
      Arbitrary(Arbitrary.arbitrary[Array[Byte]].filter(_.nonEmpty).map(new NonEmptyByteArray(_)))
  }
}

class WrappedShortArray(val array: Array[Short]) extends mutable.WrappedArray[Short] with Serializable {
  override def elemTag: ClassTag[Short] = ClassTag.Short
  override def length: Int = array.length
  override def apply(index: Int): Short = array(index)
  override def update(index: Int, elem: Short): Unit = { array(index) = elem }
}

final class NonEmptyShortArray(array: Array[Short]) extends WrappedShortArray(array) {
  require(array.nonEmpty)
}

object WrappedShortArray {
  object Conversions {
    implicit def unwrapArray(wrappedArray: WrappedShortArray): Array[Short] = wrappedArray.array

    implicit val arbNonEmptyShortArray: Arbitrary[NonEmptyShortArray] =
      Arbitrary(Arbitrary.arbitrary[Array[Short]].filter(_.nonEmpty).map(new NonEmptyShortArray(_)))
  }
}
