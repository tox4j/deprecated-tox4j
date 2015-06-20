package im.tox.tox4j.bench.picklers

object Implicits {
  implicit def classPickler[T](implicit ev: Manifest[T]): ClassPickler[T] = new ClassPickler[T](ev)
}
