package im.tox.tox4j.testing

import org.scalacheck.Arbitrary

import scala.collection.mutable
import scala.language.implicitConversions
import scala.reflect.ClassTag

class WrappedArray(val array: Array[Byte]) extends mutable.WrappedArray[Byte] with Serializable {
  override def elemTag: ClassTag[Byte] = ClassTag.Byte
  override def length: Int = array.length
  override def apply(index: Int): Byte = array(index)
  override def update(index: Int, elem: Byte): Unit = { array(index) = elem }
}

final class NonEmptyByteArray(array: Array[Byte]) extends WrappedArray(array) {
  require(array.nonEmpty)
}

object WrappedArray {
  object Conversions {
    implicit def unwrapArray(wrappedArray: WrappedArray): Array[Byte] = wrappedArray.array

    implicit val arbNonEmptyByteArray: Arbitrary[NonEmptyByteArray] =
      Arbitrary(Arbitrary.arbitrary[Array[Byte]].filter(_.nonEmpty).map(new NonEmptyByteArray(_)))
  }
}
