package im.tox.tox4j.lint

import org.brianmckenna.wartremover.{WartTraverser, WartUniverse}

/**
 * Checks whether any direct comparison with [[None]] is used. This is
 * discouraged, and [[Option.isEmpty]] or [[Option.isDefined]] should be
 * preferred, instead.
 *
 * This checker works even if [[None]] is not used as literal but hidden
 * in a name, but not if the name has type [[Option]].
 */
@SuppressWarnings(Array("org.brianmckenna.wartremover.warts.Any", "org.brianmckenna.wartremover.warts.AsInstanceOf"))
object EqualsNone extends WartTraverser {

  val errorMessage = "Avoid comparing with None; use .isEmpty or .isDefined, instead. Use `eq` if you are sure."

  def apply(u: WartUniverse): u.universe.Traverser = {
    import u.universe._

    val NoneType = rootMirror.typeOf[scala.None.type]
    val equalityOperators = Seq(
      TermName("==").encodedName,
      TermName("!=").encodedName
    )

    new u.Traverser {

      override def traverse(tree: Tree): Unit = {
        tree match {
          case Select(none, operator) =>
            errorIfNoneCompare(operator, none)
          case Apply(Select(_, operator), List(none)) =>
            errorIfNoneCompare(operator, none)

          case _ =>
        }
        super.traverse(tree)
      }

      /**
       * Checks whether the operator is == or != and the argument is scala.None.
       * If these conditions hold, this function flags the None as an error.
       *
       * @param operator The method name.
       * @param none The expression used either as argument to the operator method or
       *             as the source on which the method is invoked.
       */
      def errorIfNoneCompare(operator: u.universe.Name, none: u.universe.Tree): Unit = {
        if (equalityOperators.contains(operator) && none.tpe =:= NoneType) {
          u.error(none.pos, errorMessage)
        }
      }
    }
  }

}
