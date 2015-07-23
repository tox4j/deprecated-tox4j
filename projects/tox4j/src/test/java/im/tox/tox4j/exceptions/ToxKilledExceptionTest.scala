package im.tox.tox4j.exceptions

import im.tox.tox4j.core.ToxCoreFactory.withTox
import org.scalatest.FunSuite

final class ToxKilledExceptionTest extends FunSuite {

  test("UseAfterCloseInOrder") {
    intercept[ToxKilledException] {
      withTox { tox1 =>
        withTox { tox2 =>
          tox1.close()
          tox1.iterationInterval
        }
      }
    }
  }

  test("UseAfterCloseReverseOrder") {
    intercept[ToxKilledException] {
      withTox { tox1 =>
        withTox { tox2 =>
          tox2.close()
          tox2.iterationInterval
        }
      }
    }
  }

}
