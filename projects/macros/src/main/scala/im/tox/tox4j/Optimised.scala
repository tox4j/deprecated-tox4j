package im.tox.tox4j

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.language.existentials
import scala.language.experimental.macros
import scala.reflect.macros.whitebox
import scalaz.syntax.IdOps

/**
 * Macro annotation to transform all [[IdOps.|>]] reverse-application operator calls to
 * a direct application.
 *
 * The original |> operator cannot be optimised to a direct application by the Scala compiler
 * even if it were marked inline, because x |> f is first desugared as x |> (x1 => f(x1)).
 * After inlining of |>, it's (x1 => f(x1))(x), which is not optimised to f(x) in Scala 2.11.
 *
 * This macro is a syntax-only macro without type information. The typer runs after the
 * desugaring phase, so a typed macro would not easily be able to perform this transformation.
 */
@compileTimeOnly("Enable macro paradise to expand macro annotations")
final class Optimised extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro Optimised.impl
}

@SuppressWarnings(Array(
  "org.brianmckenna.wartremover.warts.Any",
  "org.brianmckenna.wartremover.warts.AsInstanceOf",
  "org.brianmckenna.wartremover.warts.IsInstanceOf"
))
object Optimised {

  private final case class Transform[C <: whitebox.Context](c: C) {
    import c.universe._

    def transformTree(input: Tree): Tree = { // scalastyle:ignore cyclomatic.complexity method.length
      input match {
        case Apply(Select(lhs, TermName("$bar$greater")), List(rhs)) =>
          // TODO(iphydf): Optimising non-scalaz |> to direct application is dangerous, but I can't find
          // a way to get enough type information to determine whether it's safe without the Scala
          // compiler crashing.
          Apply(
            transformTree(rhs),
            List(transformTree(lhs))
          )

        case ClassDef(mods, name, tparams, Template(parents, self, body)) =>
          ClassDef(
            mods,
            name,
            tparams,
            Template(
              parents,
              self,
              body.map(x => transformTree(x))
            )
          )

        case DefDef(mods, name, tparams, vparamss, tpt, rhs) =>
          DefDef(
            mods,
            name,
            tparams,
            vparamss,
            tpt,
            transformTree(rhs)
          )

        case Apply(fun, args) =>
          Apply(
            transformTree(fun),
            args.map(x => transformTree(x))
          )

        case Select(qualifier, name) =>
          Select(
            transformTree(qualifier),
            name
          )

        case Block(stats, expr) =>
          Block(
            stats.map(x => transformTree(x)),
            expr
          )

        case ValDef(mods, name, tpt, rhs) =>
          ValDef(
            mods,
            name,
            tpt,
            transformTree(rhs)
          )

        case TypeApply(fun, args) =>
          TypeApply(
            transformTree(fun),
            args.map(x => transformTree(x))
          )

        case _ =>
          input
      }
    }

  }

  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    val inputs = annottees.map(_.tree).toList

    val (annottee, expandees) = inputs match {
      case (param: ValDef) :: (rest @ (_ :: _))  => (param, rest)
      case (param: TypeDef) :: (rest @ (_ :: _)) => (param, rest)
      case _                                     => (EmptyTree, inputs)
    }

    val input = Block(expandees, Literal(Constant(())))
    val output = Transform[c.type](c).transformTree(input)
    c.Expr[Any](output)
  }

}
