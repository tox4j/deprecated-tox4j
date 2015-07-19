package im.tox.tox4j.impl.jni

import im.tox.tox4j.crypto.ToxCryptoTest

import scala.util.Random

final class ToxCryptoImplTest extends ToxCryptoTest(ToxCryptoImpl)

object Foo {

  sealed trait Base
  final case class A(int: Int) extends Base
  final case class B(int: Int) extends Base
  final case class C(int: Int) extends Base
  final case class D(int: Int) extends Base
  final case class E(int: Int) extends Base

  def foo(base: Base): Int = {
    base match {
      case A(x) => x + 1
      case B(x) => x + 2
      case C(x) => x + 3
      case D(x) => x + 4
      case E(x) => x + 5
    }
  }

  def main(args: Array[String]): Unit = {
    val cases = Array(A(1), B(2), C(3), D(4), E(5))
    val random = new Random
    (0 to 100000) foreach { i =>
      foo(cases(random.nextInt(cases.length)))
    }
  }

}
