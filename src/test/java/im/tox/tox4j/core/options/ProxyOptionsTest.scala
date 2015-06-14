package im.tox.tox4j.core.options

import org.scalatest.FlatSpec

final class ProxyOptionsTest extends FlatSpec {

  "proxy options" should "not allow negative ports" in {
    intercept[IllegalArgumentException] {
      ProxyOptions.Http("localhost", -1)
    }
  }

  it should "allow the port to be 0" in {
    ProxyOptions.Socks5("localhost", 0)
  }

  it should "not allow the port to be greater than 65535" in {
    ProxyOptions.Socks5("localhost", 65535)
    intercept[IllegalArgumentException] {
      ProxyOptions.Socks5("localhost", 65536)
    }
  }

}
