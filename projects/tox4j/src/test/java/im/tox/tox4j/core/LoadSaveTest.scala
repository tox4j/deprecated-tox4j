package im.tox.tox4j.core

import im.tox.tox4j.ToxCoreTestBase
import im.tox.tox4j.core.ToxCoreFactory.withTox
import im.tox.tox4j.core.enums.ToxUserStatus
import im.tox.tox4j.core.options.{SaveDataOptions, ToxOptions}
import org.scalatest.FunSuite

import scala.annotation.tailrec

final class LoadSaveTest extends FunSuite {

  private trait Check {
    def change(tox: ToxCore[Unit]): Boolean
    def check(tox: ToxCore[Unit]): Unit
  }

  @tailrec
  private def testLoadSave(check: Check): Unit = {
    val (continue, data) = withTox { tox =>
      (check.change(tox), tox.getSavedata)
    }

    withTox(SaveDataOptions.ToxSave(data)) { tox =>
      check.check(tox)
    }

    if (continue) {
      testLoadSave(check)
    }
  }

  test("Name") {
    testLoadSave(new Check() {
      private var expected: Array[Byte] = null

      override def change(tox: ToxCore[Unit]): Boolean = {
        if (expected == null) {
          expected = Array.empty
        } else {
          expected = ToxCoreTestBase.randomBytes(expected.length + 1)
        }
        tox.setName(expected)
        expected.length < ToxCoreConstants.MaxNameLength
      }

      override def check(tox: ToxCore[Unit]): Unit = {
        assert(tox.getName sameElements expected)
      }
    })
  }

  test("StatusMessage") {
    testLoadSave(new Check() {
      private var expected: Array[Byte] = null

      override def change(tox: ToxCore[Unit]): Boolean = {
        if (expected == null) {
          expected = Array.empty
        } else {
          expected = ToxCoreTestBase.randomBytes(expected.length + 1)
        }
        tox.setStatusMessage(expected)
        expected.length < ToxCoreConstants.MaxNameLength
      }

      override def check(tox: ToxCore[Unit]): Unit = {
        assert(tox.getStatusMessage sameElements expected)
      }
    })
  }

  test("Status") {
    testLoadSave(new Check() {
      private var expected = ToxUserStatus.values()

      override def change(tox: ToxCore[Unit]): Boolean = {
        tox.setStatus(expected.head)
        expected.length > 1
      }

      override def check(tox: ToxCore[Unit]): Unit = {
        assert(tox.getStatus == expected.head)
        expected = expected.tail
      }
    })
  }

  test("NoSpam") {
    testLoadSave(new Check() {
      private var expected = -1

      override def change(tox: ToxCore[Unit]): Boolean = {
        expected += 1
        tox.setNospam(expected)
        expected < 100
      }

      override def check(tox: ToxCore[Unit]): Unit = {
        assert(tox.getNospam == expected)
      }
    })
  }

  test("Friend") {
    testLoadSave(new Check() {
      private var expected: Int = 1

      override def change(tox: ToxCore[Unit]): Boolean = {
        withTox { toxFriend =>
          expected = tox.addFriend(toxFriend.getAddress, "hello".getBytes)
        }
        false
      }

      override def check(tox: ToxCore[Unit]): Unit = {
        assert(tox.getFriendList.length == 1)
        assert(tox.getFriendList(0) == expected)
      }
    })
  }

  test("SaveNotEmpty") {
    withTox { tox =>
      val data = tox.getSavedata
      assert(data != null)
      assert(data.nonEmpty)
    }
  }

  test("SaveRepeatable") {
    withTox { tox =>
      assert(tox.getSavedata sameElements tox.getSavedata)
    }
  }

  test("LoadSave1") {
    withTox { tox =>
      val data = tox.getSavedata
      val data1 = withTox(SaveDataOptions.ToxSave(data)) { tox1 =>
        tox1.getSavedata
      }
      val data2 = withTox(SaveDataOptions.ToxSave(data)) { tox2 =>
        tox2.getSavedata
      }
      assert(data1 sameElements data2)
    }
  }

  test("LoadSave2") {
    withTox { tox =>
      val data = tox.getSavedata
      withTox(SaveDataOptions.ToxSave(data)) { tox1 =>
        assert(tox1.getSavedata.length == data.length)
      }
    }
  }

  test("LoadSave3") {
    withTox { tox =>
      val data = tox.getSavedata
      withTox(SaveDataOptions.ToxSave(data)) { tox1 =>
        assert(tox1.getSavedata sameElements data)
      }
    }
  }

  test("LoadSave4") {
    withTox { tox1 =>
      val data = tox1.getSecretKey
      withTox(SaveDataOptions.SecretKey(data)) { tox2 =>
        assert(tox1.getSecretKey sameElements tox2.getSecretKey)
        assert(tox1.getPublicKey sameElements tox2.getPublicKey)
      }
    }
  }

  test("LoadSave5") {
    withTox { tox1 =>
      val data = tox1.getSecretKey
      withTox(tox1.load(ToxOptions(saveData = SaveDataOptions.SecretKey(data)))) { tox2 =>
        assert(tox1.getSecretKey sameElements tox2.getSecretKey)
        assert(tox1.getPublicKey sameElements tox2.getPublicKey)
      }
    }
  }

}
