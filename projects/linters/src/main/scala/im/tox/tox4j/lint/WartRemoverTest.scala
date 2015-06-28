package im.tox.tox4j.lint

import org.brianmckenna.wartremover.{ WartTraverser, WartUniverse }

import scala.collection.mutable.ArrayBuffer
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

@SuppressWarnings(Array("org.brianmckenna.wartremover.warts.Nothing"))
object WartRemoverTest {
  final case class Result(errors: List[String], warnings: List[String])

  def apply(t: WartTraverser)(a: Any): Result = macro applyImpl
  def applyImpl(c: whitebox.Context)(t: c.Expr[WartTraverser])(a: c.Expr[Any]): c.Expr[Nothing] = {
    import c.universe._

    val traverser = c.eval[WartTraverser](c.Expr(c.untypecheck(t.tree.duplicate)))

    val errors = new ArrayBuffer[String]
    val warnings = new ArrayBuffer[String]

    object MacroTestUniverse extends WartUniverse {
      val universe: c.universe.type = c.universe
      def error(pos: universe.Position, message: String) = errors += message
      def warning(pos: universe.Position, message: String) = warnings += message
    }

    traverser(MacroTestUniverse).traverse(a.tree)

    c.Expr(q"WartRemoverTest.Result(List(..${errors.toList}), List(..${warnings.toList}))")
  }
}
