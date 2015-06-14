package im.tox.tox4j.testing.autotest

import com.typesafe.scalalogging.Logger
import im.tox.tox4j.ToxCoreImplTestBase
import im.tox.tox4j.core.ToxCore
import org.slf4j.LoggerFactory

object AliceBobTestBase {
  val FRIEND_NUMBER = 10
}

abstract class AliceBobTestBase extends ToxCoreImplTestBase {

  protected val logger = Logger(LoggerFactory.getLogger(classOf[AliceBobTestBase]))

  protected def newAlice(name: String, expectedFriendName: String): ChatClient
  protected def newBob(name: String, expectedFriendName: String): ChatClient = newAlice(name, expectedFriendName)

  def getTopLevelMethod(stackTrace: Array[StackTraceElement]): String = {
    stackTrace
      .filter(_.getClassName == classOf[AliceBobTest].getName)
      .last
      .getMethodName
  }

  protected def runAliceBobTest(withTox: => (ToxCore => Unit) => Unit): Unit = {
    val method = getTopLevelMethod(Thread.currentThread.getStackTrace)
    logger.info(s"[${Thread.currentThread.getId}] --- ${getClass.getSimpleName}.$method")

    val aliceChat = newAlice("Alice", "Bob")
    val bobChat = newBob("Bob", "Alice")

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

        while (aliceChat.isChatting || bobChat.isChatting) {
          alice.iterate()
          bob.iterate()

          aliceChat.performTasks(alice)
          bobChat.performTasks(bob)

          val interval = Math.max(alice.iterationInterval, bob.iterationInterval)
          Thread.sleep(interval)
        }

        aliceChat.exit()
        bobChat.exit()
      }
    }
  }
}
