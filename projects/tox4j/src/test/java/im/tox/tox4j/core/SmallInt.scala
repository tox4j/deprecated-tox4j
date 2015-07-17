package im.tox.tox4j.core

import org.scalacheck.{ Arbitrary, Gen }

import scala.language.implicitConversions
import scala.runtime.IntegralProxy

final case class SmallInt(self: Int) extends AnyVal with IntegralProxy[Int] {
  override protected def num = scala.math.Numeric.IntIsIntegral
  override protected def ord = scala.math.Ordering.Int
  override def isWhole(): Boolean = true
}

object SmallInt {
  implicit val arbSmallInt: Arbitrary[SmallInt] = Arbitrary(Gen.choose(0, 100).map(apply))
  implicit def smallIntToInt(smallInt: SmallInt): Int = smallInt.self
}
