package net.sandrogrzicic.scalabuff.compiler

import scala.annotation.tailrec
import scala.language.implicitConversions

/**
 * Viktor Klang's Enum
 * Source: https://gist.github.com/1057513/
 */
trait Enum {

  import java.util.concurrent.atomic.AtomicReference

  type EnumVal <: Value

  private val _values = new AtomicReference(Vector[EnumVal]())

  /**
   * Add an EnumVal to our storage, using CCAS to make sure it's thread safe, returns the ordinal.
   */
  @tailrec
  private final def addEnumVal(newVal: EnumVal): Int = {
    import _values.{ compareAndSet => CAS, get }
    val oldVec = get
    val newVec = oldVec :+ newVal
    if ((get eq oldVec) && CAS(oldVec, newVec)) newVec.indexWhere(_ eq newVal) else addEnumVal(newVal)
  }

  /**
   * Get all the enums that exist for this type.
   */
  def values: Vector[EnumVal] = _values.get

  protected trait Value {
    self: EnumVal => // Enforce that no one mixes in Value in a non-EnumVal type
    final val ordinal = addEnumVal(this) // Adds the EnumVal and returns the ordinal

    def name: String

    override def toString = name
    override def equals(other: Any) = this eq other.asInstanceOf[AnyRef]
    override def hashCode = 31 * (this.getClass.## + name.## + ordinal)
  }

}

