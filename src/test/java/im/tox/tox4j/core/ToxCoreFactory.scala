package im.tox.tox4j.core

import im.tox.tox4j.core.exceptions.ToxNewException
import im.tox.tox4j.core.options.{ ProxyOptions, ToxOptions, SaveDataOptions }
import im.tox.tox4j.impl.jni.ToxCoreImpl

import scala.collection.mutable.ArrayBuffer

object ToxCoreFactory {

  private final val toxes = new ArrayBuffer[ToxCore]

  def make(options: ToxOptions = ToxOptions()): ToxCore = {
    try {
      new ToxCoreImpl(options)
    } catch {
      case e: ToxNewException if e.code == ToxNewException.Code.PORT_ALLOC =>
        System.gc()
        new ToxCoreImpl(options)
    }
  }

  def makeList(count: Int, options: ToxOptions = ToxOptions()): ToxList = {
    new ToxList(() => { this(options) }, count)
  }

  def destroyAll(): Unit = {
    toxes.foreach(_.close())
    toxes.clear()
    System.gc()
  }

  def apply(options: ToxOptions): ToxCore = {
    val tox = make(options)
    toxes += tox
    tox
  }

  def withTox[R](options: ToxOptions)(f: ToxCore => R): R = {
    val tox = make(options)
    try {
      f(tox)
    } finally {
      tox.close()
    }
  }

  def withTox[R](ipv6Enabled: Boolean, udpEnabled: Boolean, proxy: ProxyOptions.Type)(f: ToxCore => R): R = {
    withTox(ToxOptions(ipv6Enabled, udpEnabled, proxy))(f)
  }

  def withTox[R](ipv6Enabled: Boolean, udpEnabled: Boolean)(f: ToxCore => R): R = {
    withTox(ToxOptions(ipv6Enabled, udpEnabled))(f)
  }

  def withTox[R](saveData: SaveDataOptions.Type)(f: ToxCore => R): R = {
    withTox(new ToxOptions(saveData = saveData))(f);
  }

  def withTox[R](f: ToxCore => R): R = {
    withTox(ipv6Enabled = true, udpEnabled = true)(f)
  }

  def withToxes[R](count: Int, options: ToxOptions)(f: ToxList => R): R = {
    val toxes = makeList(count, options)
    try {
      f(toxes)
    } finally {
      toxes.close()
    }
  }

  def withToxes[R](count: Int)(f: ToxList => R): R = {
    withToxes(count, ToxOptions())(f)
  }

}
