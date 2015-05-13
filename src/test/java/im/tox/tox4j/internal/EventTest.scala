package im.tox.tox4j.internal

import org.scalatest.FlatSpec

class EventTest extends FlatSpec {

  "Callback" should "be called on run()" in {
    val event = new Event
    var called = false

    val id = event.add(new Runnable {
      override def run(): Unit = called = true
    })

    event.run()
    assert(called)
  }


  it should "not be called if it was deleted" in {
    val event = new Event
    var called = false

    val id = event.add(new Runnable {
      override def run(): Unit = called = true
    })

    event.remove(id)

    event.run()
    assert(!called)
  }


  "remove" should "be idempotent" in {
    val event = new Event
    var called = 0

    val id1 = event.add(new Runnable {
      override def run(): Unit = called = 1
    })
    val id2 = event.add(new Runnable {
      override def run(): Unit = called = 2
    })

    event.remove(id1)
    event.remove(id1)

    event.run()
    assert(called == 2)
  }

}