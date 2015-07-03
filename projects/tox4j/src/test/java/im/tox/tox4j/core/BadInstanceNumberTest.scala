package im.tox.tox4j.core

import im.tox.tox4j.exceptions.ToxKilledException
import org.scalatest.FunSuite
import org.scalatest.prop.PropertyChecks

/**
 * This class tests whether the C++ code is resilient against memory corruption
 * and bad people using reflection to forge invalid Tox instances.
 */
final class BadInstanceNumberTest extends FunSuite with PropertyChecks {

  private def callWithInstanceNumber(instanceNumber: Int): Unit = {
    ToxCoreFactory.withTox { tox =>
      val field = tox.getClass.getDeclaredField("instanceNumber")
      field.setAccessible(true)
      val oldInstanceNumber = field.get(tox).asInstanceOf[Int]
      field.set(tox, instanceNumber)
      val exception =
        try {
          tox.iterationInterval
          null
        } catch {
          case e: Throwable => e
        }
      // Set it back to the good one, so close() works.
      field.set(tox, oldInstanceNumber)
      if (exception != null) {
        throw exception
      }
    }
  }

  test("negative instance numbers") {
    forAll { (instanceNumber: Int) =>
      whenever(instanceNumber <= 0) {
        intercept[IllegalStateException] {
          callWithInstanceNumber(instanceNumber)
        }
      }
    }
  }

  test("very large instance numbers") {
    forAll { (instanceNumber: Int) =>
      whenever(instanceNumber >= 0xffff) {
        intercept[IllegalStateException] {
          callWithInstanceNumber(instanceNumber)
        }
      }
    }
  }

  test("any invalid instance numbers") {
    // This could be fine if there is another Tox instance lingering around, but we assume there isn't.
    // So, it's either killed (ToxKilledException) or never existed (IllegalStateException).
    System.gc() // After this, there should be no lingering instances.

    forAll { (instanceNumber: Int) =>
      whenever(instanceNumber != 1) {
        try {
          callWithInstanceNumber(instanceNumber)
          fail("No exception thrown. Expected IllegalStateException or ToxKilledException.")
        } catch {
          case _: IllegalStateException =>
          case _: ToxKilledException    => // Both fine.
        }
      }
    }
  }

}
