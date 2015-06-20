package im.tox.tox4j.bench.picklers

import org.scalameter.picklers.Implicits._
import org.scalameter.picklers.Pickler

final class ClassPickler[T](evidence: Manifest[T]) extends Pickler[Class[T]] {

  override def pickle(x: Class[T]): Array[Byte] = {
    implicitly[Pickler[String]].pickle(x.getCanonicalName)
  }

  override def unpickle(a: Array[Byte], from: Int): (Class[T], Int) = {
    val (className, next) = implicitly[Pickler[String]].unpickle(a, from)
    val unpickledClass = Class.forName(className)
    val unpickledClassTag = Manifest.classType[T](unpickledClass)
    assert(unpickledClassTag == evidence)
    (unpickledClass.asInstanceOf[Class[T]], next)
  }

}
