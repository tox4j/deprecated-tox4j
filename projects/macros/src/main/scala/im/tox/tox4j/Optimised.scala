package im.tox.tox4j

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.language.experimental.macros
import scala.reflect.macros.whitebox
import scalaz.syntax.IdOps

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

  def transformTemplate(c: whitebox.Context)(input: c.universe.Template): c.universe.Template = {
    input
  }

  def transformTree(c: whitebox.Context)(input: c.universe.Tree): c.universe.Tree = { // scalastyle:ignore cyclomatic.complexity method.length
    import c.universe._

    val BarGreaterTerm = TermName("$bar$greater")
    val ReverseApplyOperator = typeOf[IdOps[_]].member(BarGreaterTerm).asTerm

    input match {
      case Apply(Select(lhs, ReverseApplyOperator), List(rhs)) =>
        Apply(
          transformTree(c)(rhs),
          List(transformTree(c)(lhs))
        )

      case Apply(Select(lhs, BarGreaterTerm), List(rhs)) =>
        c.warning(input.pos, "Optimising non-scalaz |> to direct application")
        Apply(
          transformTree(c)(rhs),
          List(transformTree(c)(lhs))
        )

      case ClassDef(mods, name, tparams, Template(parents, self, body)) =>
        ClassDef(
          mods,
          name,
          tparams,
          Template(
            parents,
            self,
            body.map(x => transformTree(c)(x))
          )
        )

      case DefDef(mods, name, tparams, vparamss, tpt, rhs) =>
        DefDef(
          mods,
          name,
          tparams,
          vparamss,
          tpt,
          transformTree(c)(rhs)
        )

      case Apply(fun, args) =>
        Apply(
          transformTree(c)(fun),
          args.map(x => transformTree(c)(x))
        )

      case Select(qualifier, name) =>
        Select(
          transformTree(c)(qualifier),
          name
        )

      case Block(stats, expr) =>
        Block(
          stats.map(x => transformTree(c)(x)),
          expr
        )

      case ValDef(mods, name, tpt, rhs) =>
        ValDef(
          mods,
          name,
          tpt,
          transformTree(c)(rhs)
        )

      case TypeApply(fun, args) =>
        TypeApply(
          transformTree(c)(fun),
          args.map(x => transformTree(c)(x))
        )

      case _ =>
        input
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
    val output = transformTree(c)(input)
    c.Expr[Any](output)
  }

}
