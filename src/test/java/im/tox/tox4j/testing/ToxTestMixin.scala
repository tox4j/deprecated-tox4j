package im.tox.tox4j.testing

import im.tox.tox4j.core.{ ToxCore, ToxCoreFactory }
import im.tox.tox4j.exceptions.ToxException
import org.junit.Assert._

trait ToxTestMixin {

  protected def intercept(code: Enum[_])(f: => Unit) = {
    try {
      f
      fail("Expected exception with code " + code.name())
    } catch {
      case e: ToxException[_] =>
        assertEquals(code, e.code)
    }
  }

  protected def interceptWithTox(code: Enum[_])(f: ToxCore => Unit) = {
    intercept(code) {
      ToxCoreFactory.withTox(f)
    }
  }

}
