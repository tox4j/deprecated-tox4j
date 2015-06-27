package im.tox.tox4j.testing.autotest

import com.typesafe.scalalogging.Logger
import im.tox.tox4j.core.ToxCore
import im.tox.tox4j.testing.ToxTestMixin
import org.scalatest.FunSuite
import org.slf4j.LoggerFactory

import scala.annotation.tailrec

object AliceBobTestBase {
  val FRIEND_NUMBER = 10
}

abstract class AliceBobTestBase extends FunSuite with ToxTestMixin {

  protected val logger = Logger(LoggerFactory.getLogger(classOf[AliceBobTestBase]))

  protected def newAlice(name: String, expectedFriendName: String): ChatClient

  private def getTopLevelMethod(stackTrace: Array[StackTraceElement]): String = {
    stackTrace
      .filter(_.getClassName == classOf[AliceBobTest].getName)
      .lastOption
      .fold("<unknown>")(_.getMethodName)
  }

  @tailrec
  private def mainLoop(clients: Seq[(ToxCore, ChatClient)]): Unit = {
    clients.foreach { case (tox, client) => tox.iterate() }
    clients.foreach { case (tox, client) => client.performTasks(tox) }

    val interval = clients.map { case (tox, client) => tox.iterationInterval }.max
    Thread.sleep(interval)

    if (clients.exists { case (tox, client) => client.isChatting }) {
      mainLoop(clients)
    }
  }

  protected def runAliceBobTest(withTox: => (ToxCore => Unit) => Unit): Unit = {
    val method = getTopLevelMethod(Thread.currentThread.getStackTrace)
    logger.info(s"[${Thread.currentThread.getId}] --- ${getClass.getSimpleName}.$method")

    val aliceChat = newAlice("Alice", "Bob")
    val bobChat = newAlice("Bob", "Alice")

    withTox { alice =>
      withTox { bob =>
        assert(alice ne bob)

        addFriends(alice, AliceBobTestBase.FRIEND_NUMBER)
        addFriends(bob, AliceBobTestBase.FRIEND_NUMBER)

        alice.addFriendNoRequest(bob.getPublicKey)
        bob.addFriendNoRequest(alice.getPublicKey)

        aliceChat.expectedFriendAddress = bob.getAddress
        bobChat.expectedFriendAddress = alice.getAddress

        alice.callback(aliceChat)
        bob.callback(bobChat)

        aliceChat.setup(alice)
        bobChat.setup(bob)

        mainLoop(Seq(
          (alice, aliceChat),
          (bob, bobChat)
        ))

        aliceChat.exit()
        bobChat.exit()
      }
    }
  }
}
