package im.tox.tox4j.core

import org.scalacheck.{Arbitrary, Gen}

import scala.language.implicitConversions
import scala.runtime.IntegralProxy

final case class SmallNat(self: Int) extends AnyVal with IntegralProxy[Int] {
  override protected def num = scala.math.Numeric.IntIsIntegral
  override protected def ord = scala.math.Ordering.Int
  override def isWhole(): Boolean = true
}

object SmallNat {
  implicit val arbSmallNat: Arbitrary[SmallNat] = Arbitrary(Gen.choose(0, 100).map(apply))
  implicit def smallNatToInt(smallNat: SmallNat): Int = smallNat.self
}
