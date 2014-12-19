package im.tox.client

import im.tox.tox4j.enums.ToxStatus

class ToxClientSpec extends org.scalatest.FlatSpec {

  "getName" should "return the name set by setName" in {
    val tox = new ToxClient
    tox.setName("Alice")
    assert(tox.getName == "Alice")
  }

  "getStatus" should "return the status set by setStatus" in {
    val tox = new ToxClient
    assert(tox.getStatus == ToxStatus.NONE)
    tox.setStatus(ToxStatus.AWAY)
    assert(tox.getStatus == ToxStatus.AWAY)
  }

  "getStatusMessage" should "return the status message set by setStatusMessage" in {
    val tox = new ToxClient
    tox.setStatusMessage("Yo, cool status")
    assert(tox.getStatusMessage == "Yo, cool status")
  }

}
