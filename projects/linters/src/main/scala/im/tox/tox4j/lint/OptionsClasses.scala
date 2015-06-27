package im.tox.tox4j.lint

import org.brianmckenna.wartremover.{ WartTraverser, WartUniverse }

@SuppressWarnings(Array("org.brianmckenna.wartremover.warts.Any"))
object OptionsClasses extends WartTraverser {

  def errorDefaultArguments: String = "Non-ADT options case class must have default arguments for all parameters"
  def errorCaseClass: String = "Options classes must be case classes"
  def errorBadParent(parentName: String): String = s"Options classes can only inherit from options traits: $parentName not allowed"

  def apply(u: WartUniverse): u.universe.Traverser = {
    import u.universe._

    new u.universe.Traverser {

      def isTox4jType(symbol: Symbol): Boolean = {
        Seq("av", "core") exists { pkg =>
          symbol.fullName.startsWith(s"im.tox.tox4j.$pkg.options")
        }
      }

      def getConstructorParams(tree: Tree): Option[List[ValDef]] = {
        tree match {
          case DefDef(mods, termNames.CONSTRUCTOR, tparams, List(vparams), tpt, rhs) =>
            Some(vparams)
          case _ =>
            None
        }
      }

      def checkCaseClass(parents: List[u.universe.Tree], body: List[u.universe.Tree]): Unit = {
        if (!parents.exists(parent => isTox4jType(parent.tpe.typeSymbol))) {
          body.flatMap(x => getConstructorParams(x)).flatten.filter(_.rhs.isEmpty) foreach { param =>
            u.error(param.pos, errorDefaultArguments)
          }
        }
      }

      override def traverse(tree: Tree): Unit = {
        tree match {
          case ClassDef(mods, typeName, _, Template(parents, self, body)) if isTox4jType(tree.symbol) =>

            parents.foreach { parent =>
              val typeSymbol = parent.tpe.typeSymbol
              if (!isTox4jType(typeSymbol)) {
                typeSymbol.fullName match {
                  case "java.lang.Object" =>
                  case "scala.Product" =>
                  case "scala.Serializable" =>
                  case x =>
                    u.error(tree.pos, errorBadParent(x))
                }
              }
            }

            if (mods.hasFlag(Flag.CASE)) {
              checkCaseClass(parents, body)
            } else if (!mods.hasFlag(Flag.TRAIT)) {
              u.error(tree.pos, errorCaseClass)
            }

          case _ =>
        }
        super.traverse(tree)
      }

    }
  }

}
