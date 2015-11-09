package im.tox.core.random

import org.scalacheck.Gen
import org.scalatest.WordSpec
import org.scalatest.prop.PropertyChecks

final class RandomCoreTest extends WordSpec with PropertyChecks {

  "random bytes" should {
    "have high entropy" in {
      forAll(Gen.choose(40, 500)) { length =>
        assert(RandomCore.entropy(RandomCore.randomBytes(length)) > 0.5)
      }
    }
  }

  "random longs" should {
    "have a random distribution" in {
      forAll(Gen.choose(500, 10000)) { count =>
        val values = (0 to count).map({ _ => RandomCore.randomLong() }).sorted
        val distances =
          for (index <- values.indices.drop(1)) yield {
            (values(index) - values(index - 1)).toDouble
          }
        val averageDistance = distances.sum / distances.length
        assert(averageDistance > Long.MaxValue / count)
      }
    }
  }

  "entropy" should {
    "be 0 for the empty sequence" in {
      assert(RandomCore.entropy(Nil) == 0)
    }

    "be 0 for sequences of all the same element" in {
      forAll { (bytes: Seq[Byte], filler: Byte) =>
        assert(RandomCore.entropy(bytes.map(_ => filler)) == 0)
      }
    }

    "be near 1 for long sequences" in {
      assert(RandomCore.entropy((0 to 1000).map(_.toByte)) > 0.999)
    }

    "be 1 for sequences containing every byte equally often (maximum)" in {
      forAll(Gen.choose(1, 10)) { count =>
        val bytes = (1 to count).flatMap(_ => (0 to 255).map(_.toByte))
        assert(RandomCore.entropy(bytes) == 1)
      }
    }

    "be sorting-insensitive (symmetry)" in {
      forAll { (bytes: Seq[Byte]) =>
        assert(RandomCore.entropy(bytes) == RandomCore.entropy(bytes.sorted))
        assert(RandomCore.entropy(bytes) == RandomCore.entropy(bytes.reverse))
      }
    }
  }

}
