package im.tox.tox4j.core.bench

import im.tox.tox4j.bench.TimingReport
import im.tox.tox4j.core.{ ToxCoreConstants, ToxCoreFactory }
import im.tox.tox4j.core.callbacks.ToxEventListener

class ToxCoreTimingBench extends TimingReport {

  timing of "ToxCore" in {

    measure method "addFriend" in {
      using(friendAddresses) in { friendList =>
        ToxCoreFactory.withTox { tox =>
          friendList foreach (tox.addFriend(_, Array.ofDim(1)))
        }
      }
    }

    measure method "addFriendNoRequest" in {
      using(friendKeys) in { friendList =>
        ToxCoreFactory.withTox { tox =>
          friendList foreach tox.addFriendNoRequest
        }
      }
    }

    measure method "iterate" in {
      using(iterations) in { sz =>
        ToxCoreFactory.withTox { tox =>
          (0 until sz) foreach (_ => tox.iteration())
        }
      }
    }

    measure method "callback" in {
      using(iterations) in { sz =>
        ToxCoreFactory.withTox { tox =>
          (0 until sz) foreach (_ => tox.callback(ToxEventListener.IGNORE))
        }
      }
    }

    measure method "bootstrap" in {
      val publicKey = Array.ofDim[Byte](ToxCoreConstants.PUBLIC_KEY_SIZE)
      using(iterations) in { sz =>
        ToxCoreFactory.withTox { tox =>
          (0 until sz) foreach (_ => tox.bootstrap("localhost", 8080, publicKey))
        }
      }
    }

    measure method "addTcpRelay" in {
      val publicKey = Array.ofDim[Byte](ToxCoreConstants.PUBLIC_KEY_SIZE)
      using(iterations) in { sz =>
        ToxCoreFactory.withTox { tox =>
          (0 until sz) foreach (_ => tox.addTcpRelay("localhost", 8080, publicKey))
        }
      }
    }

    measure method "getAddress" in {
      using(iterations) in { sz =>
        ToxCoreFactory.withTox { tox =>
          (0 until sz) foreach (_ => tox.getAddress)
        }
      }
    }

    measure method "getDhtId" in {
      using(iterations) in { sz =>
        ToxCoreFactory.withTox { tox =>
          (0 until sz) foreach (_ => tox.getDhtId)
        }
      }
    }

    measure method "getNoSpam" in {
      using(iterations) in { sz =>
        ToxCoreFactory.withTox { tox =>
          (0 until sz) foreach (_ => tox.getNoSpam)
        }
      }
    }

    measure method "getName" in {
      using(iterations) in { sz =>
        ToxCoreFactory.withTox { tox =>
          (0 until sz) foreach (_ => tox.getName)
        }
      }
    }

    measure method "getPublicKey" in {
      using(iterations) in { sz =>
        ToxCoreFactory.withTox { tox =>
          (0 until sz) foreach (_ => tox.getPublicKey)
        }
      }
    }

    measure method "getSecretKey" in {
      using(iterations) in { sz =>
        ToxCoreFactory.withTox { tox =>
          (0 until sz) foreach (_ => tox.getSecretKey)
        }
      }
    }

    measure method "getStatus" in {
      using(iterations) in { sz =>
        ToxCoreFactory.withTox { tox =>
          (0 until sz) foreach (_ => tox.getStatus)
        }
      }
    }

    measure method "getStatusMessage" in {
      using(iterations) in { sz =>
        ToxCoreFactory.withTox { tox =>
          (0 until sz) foreach (_ => tox.getStatusMessage)
        }
      }
    }

    measure method "getUdpPort" in {
      using(iterations) in { sz =>
        ToxCoreFactory.withTox { tox =>
          (0 until sz) foreach (_ => tox.getUdpPort)
        }
      }
    }

    measure method "getFriendList" in {
      using(iterations) in { sz =>
        ToxCoreFactory.withTox { tox =>
          (0 until sz) foreach (_ => tox.getFriendList)
        }
      }
    }

    measure method "new+close" in {
      using(iterations) in { sz =>
        (0 until sz) foreach (_ => ToxCoreFactory.withTox { _ => })
      }
    }

  }

}
