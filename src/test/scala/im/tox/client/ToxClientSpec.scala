package im.tox.client

import im.tox.tox4j.core.enums.ToxStatus
import org.scalatest.FlatSpec

class ToxClientSpec extends FlatSpec {

  "getName" should "return the name set by setName" in {
    val tox = new ToxClient
    tox.name = "Alice"
    assert(tox.name == "Alice")
  }

  it must "return an empty string if no name was set" in {
    val tox = new ToxClient
    assert(tox.name == "")
  }


  "getStatus" should "return the status set by setStatus" in {
    val tox = new ToxClient
    assert(tox.status == ToxStatus.NONE)
    tox.status = ToxStatus.AWAY
    assert(tox.status == ToxStatus.AWAY)
  }

  it should "return NONE if no status was set" in {
    val tox = new ToxClient
    assert(tox.status == ToxStatus.NONE)
  }


  "getStatusMessage" should "return the status message set by setStatusMessage" in {
    val tox = new ToxClient
    tox.statusMessage = "Yo, cool status"
    assert(tox.statusMessage == "Yo, cool status")
  }

  it should "return an empty string if no status message was set" in {
    val tox = new ToxClient
    assert(tox.statusMessage == "")
  }

}
