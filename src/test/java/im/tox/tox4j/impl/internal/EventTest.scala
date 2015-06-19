package im.tox.tox4j.impl.internal

import org.scalatest.FlatSpec

class EventTest extends FlatSpec {

  "Callback" should "be called on run()" in {
    val event = new Event
    var called = false

    val id = event += (() => called = true)

    event()
    assert(called)
  }

  it should "not be called if it was deleted" in {
    val event = new Event
    var called = false

    val id = event += (() => called = true)

    event -= id

    event()
    assert(!called)
  }

  "remove" should "be idempotent" in {
    val event = new Event
    var called = 0

    val id1 = event += (() => called = 1)
    val id2 = event += (() => called = 2)

    event -= id1
    event -= id1

    event()
    assert(called == 2)
  }

}
