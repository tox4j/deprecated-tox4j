package im.tox.core.random

import java.nio.ByteBuffer

import im.tox.tox4j.impl.jni.ToxCryptoJni
import org.jetbrains.annotations.NotNull

object RandomCore {

  def randomBytes(length: Int): Array[Byte] = {
    val bytes = Array.ofDim[Byte](length)
    ToxCryptoJni.randombytes(bytes)
    bytes
  }

  def randomByteBuffer(length: Int): ByteBuffer = {
    val bytes = randomBytes(length)
    ByteBuffer.wrap(bytes)
  }

  def randomLong(): Long = {
    randomByteBuffer(java.lang.Long.SIZE / java.lang.Byte.SIZE).getLong
  }

  def entropy(@NotNull data: Seq[Byte]): Double = {
    val frequencies = new Array[Int](-Byte.MinValue * 2)
    for (b <- data) {
      frequencies(Byte.MaxValue - b) += 1
    }

    val probabilities =
      for (frequency <- frequencies) yield {
        if (frequency != 0) {
          val probability = frequency.toDouble / data.length
          -probability * (Math.log(probability) / Math.log(-Byte.MinValue * 2))
        } else {
          0
        }
      }
    probabilities.sum
  }

}
