package im.tox.tox4j.core

import java.util.{ ArrayList, Arrays }

import im.tox.tox4j.ToxCoreTestBase
import im.tox.tox4j.core.ToxCoreFactory.withTox
import im.tox.tox4j.core.enums.ToxUserStatus
import im.tox.tox4j.core.options.{ SaveDataOptions, ToxOptions }
import im.tox.tox4j.exceptions.ToxException
import org.junit.Assert._
import org.junit.Test

final class LoadSaveTest extends ToxCoreTestBase {

  private trait Check {
    @throws(classOf[ToxException[_]])
    def change(tox: ToxCore[Unit]): Boolean
    def check(tox: ToxCore[Unit]): Unit
  }

  private def testLoadSave(check: Check): Unit = {
    var moreTests = true
    var data: Seq[Byte] = Nil
    while (moreTests) {
      withTox { tox =>
        moreTests = check.change(tox)
        data = tox.getSavedata
      }

      withTox(SaveDataOptions.ToxSave(data)) { tox =>
        check.check(tox)
      }
    }
  }

  @Test def testName(): Unit = {
    testLoadSave(new Check() {
      private var expected: Array[Byte] = null

      @throws(classOf[ToxException[_]])
      override def change(tox: ToxCore[Unit]): Boolean = {
        if (expected == null) {
          expected = Array[Byte]()
        } else {
          expected = ToxCoreTestBase.randomBytes(expected.length + 1)
        }
        tox.setName(expected)
        expected.length < ToxCoreConstants.MAX_NAME_LENGTH
      }

      override def check(tox: ToxCore[Unit]): Unit = {
        assertArrayEquals(expected, tox.getName)
      }
    })
  }

  @Test def testStatusMessage(): Unit = {
    testLoadSave(new Check() {
      private var expected: Array[Byte] = null

      @throws(classOf[ToxException[_]])
      override def change(tox: ToxCore[Unit]): Boolean = {
        if (expected == null) {
          expected = Array[Byte]()
        } else {
          expected = ToxCoreTestBase.randomBytes(expected.length + 1)
        }
        tox.setStatusMessage(expected)
        expected.length < ToxCoreConstants.MAX_NAME_LENGTH
      }

      override def check(tox: ToxCore[Unit]): Unit = {
        assertArrayEquals(expected, tox.getStatusMessage)
      }
    })
  }

  @Test def testStatus(): Unit = {
    testLoadSave(new Check() {

      private val expected = new ArrayList(Arrays.asList(ToxUserStatus.values(): _*))

      @throws(classOf[ToxException[_]])
      override def change(tox: ToxCore[Unit]): Boolean = {
        tox.setStatus(expected.get(expected.size() - 1))
        expected.size() > 1
      }

      override def check(tox: ToxCore[Unit]): Unit = {
        assertEquals(expected.remove(expected.size() - 1), tox.getStatus)
      }
    })
  }

  @Test def testNoSpam(): Unit = {
    testLoadSave(new Check() {
      private var expected = -1

      @throws(classOf[ToxException[_]])
      override def change(tox: ToxCore[Unit]): Boolean = {
        expected += 1
        tox.setNospam(expected)
        expected < 100
      }

      override def check(tox: ToxCore[Unit]): Unit = {
        assertEquals(expected, tox.getNospam)
      }
    })
  }

  @Test def testFriend(): Unit = {
    testLoadSave(new Check() {
      private var expected: Int = 1

      @throws(classOf[ToxException[_]])
      override def change(tox: ToxCore[Unit]): Boolean = {
        withTox { toxFriend =>
          expected = tox.addFriend(toxFriend.getAddress, "hello".getBytes)
        }
        false
      }

      override def check(tox: ToxCore[Unit]): Unit = {
        assertEquals(1, tox.getFriendList.length)
        assertEquals(expected, tox.getFriendList(0))
      }
    })
  }

  @Test def testSaveNotEmpty(): Unit = {
    withTox { tox =>
      var data = tox.getSavedata
      assertNotNull(data)
      assertNotEquals(0, data.length)
    }
  }

  @Test def testSaveRepeatable(): Unit = {
    withTox { tox =>
      assertArrayEquals(tox.getSavedata, tox.getSavedata)
    }
  }

  @Test def testLoadSave1(): Unit = {
    withTox { tox =>
      var data = tox.getSavedata
      var data1: Array[Byte] = new Array[Byte](0)
      var data2: Array[Byte] = new Array[Byte](0)
      withTox(SaveDataOptions.ToxSave(data)) { tox1 =>
        data1 = tox1.getSavedata
      }
      withTox(SaveDataOptions.ToxSave(data)) { tox2 =>
        data2 = tox2.getSavedata
      }
      assertArrayEquals(data1, data2)
    }
  }

  @Test def testLoadSave2(): Unit = {
    withTox { tox =>
      var data = tox.getSavedata
      withTox(SaveDataOptions.ToxSave(data)) { tox1 =>
        assertEquals(data.length, tox1.getSavedata.length)
      }
    }
  }

  @Test def testLoadSave3(): Unit = {
    withTox { tox =>
      var data = tox.getSavedata
      withTox(SaveDataOptions.ToxSave(data)) { tox1 =>
        assertArrayEquals(data, tox1.getSavedata)
      }
    }
  }

  @Test def testLoadSave4(): Unit = {
    withTox { tox1 =>
      var data = tox1.getSecretKey
      withTox(SaveDataOptions.SecretKey(data)) { tox2 =>
        assertArrayEquals(tox1.getSecretKey, tox2.getSecretKey)
        assertArrayEquals(tox1.getPublicKey, tox2.getPublicKey)
      }
    }
  }

  @Test def testLoadSave5(): Unit = {
    withTox { tox1 =>
      var data = tox1.getSecretKey
      withTox(tox1.load(ToxOptions(saveData = SaveDataOptions.SecretKey(data)))) { tox2 =>
        assertArrayEquals(tox1.getSecretKey, tox2.getSecretKey)
        assertArrayEquals(tox1.getPublicKey, tox2.getPublicKey)
      }
    }
  }

}
