package im.tox.tox4j.lint

import org.brianmckenna.wartremover.WartTraverser
import org.brianmckenna.wartremover.WartUniverse

import language.experimental.macros
import reflect.macros.whitebox
import scala.collection.mutable.ArrayBuffer

@SuppressWarnings(Array("org.brianmckenna.wartremover.warts.Nothing"))
object WartRemoverTest {
  final case class Result(errors: List[String], warnings: List[String])

  def apply(t: WartTraverser)(a: Any): Result = macro applyImpl
  def applyImpl(c: whitebox.Context)(t: c.Expr[WartTraverser])(a: c.Expr[Any]) = {
    import c.universe._

    val traverser = c.eval[WartTraverser](c.Expr(c.untypecheck(t.tree.duplicate)))

    var errors = new ArrayBuffer[String]
    var warnings = new ArrayBuffer[String]

    object MacroTestUniverse extends WartUniverse {
      val universe: c.universe.type = c.universe
      def error(pos: universe.Position, message: String) = errors += message
      def warning(pos: universe.Position, message: String) = warnings += message
    }

    traverser(MacroTestUniverse).traverse(a.tree)

    c.Expr(q"WartRemoverTest.Result(List(..${errors.toList}), List(..${warnings.toList}))")
  }
}
