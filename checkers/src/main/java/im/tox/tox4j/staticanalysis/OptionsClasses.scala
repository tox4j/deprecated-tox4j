package im.tox.tox4j.staticanalysis

import org.brianmckenna.wartremover.{WartTraverser, WartUniverse}

object OptionsClasses extends WartTraverser {
  def apply(u: WartUniverse): u.universe.Traverser = {
    import u.universe._

    new u.universe.Traverser {

      def isTox4jType(symbol: Symbol): Boolean = {
        Seq("av", "core") exists (pkg => symbol.fullName.startsWith(s"im.tox.tox4j.$pkg.options"))
      }

      def getConstructorParams(tree: Tree): Option[List[ValDef]] = {
        tree match {
          case DefDef(mods, name, tparams, List(vparams), tpt, rhs)
            if name.toString == "<init>" =>
            Some(vparams)
          case x =>
            None
        }
      }

      override def traverse(tree: Tree): Unit = {
        tree match {
          case ClassDef(mods, typeName, _, Template(parents, self, body)) if isTox4jType(tree.symbol) =>

            if (mods.hasFlag(Flag.CASE)) {
              if (!parents.exists(parent => isTox4jType(parent.tpe.typeSymbol))) {
                body.flatMap(x => getConstructorParams(x)).flatten.filter(_.rhs.isEmpty) foreach { param =>
                  u.error(param.pos, "Non-ADT options case class must have default arguments for all parameters")
                }
              }
            } else if (!mods.hasFlag(Flag.TRAIT)) {
              u.error(tree.pos, "Options classes must be case classes")
            }

          case _ =>
        }
        super.traverse(tree)
      }

    }
  }
}
