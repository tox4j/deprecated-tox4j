open ApiAst
open ApiMap


let map_decl v symtab = function
  | Decl_Const (name, expr) ->
      let macro =
        let expr =
          ApiMap.visit_expr ScopeBinding.Inverse.v symtab expr
        in

        Macro (
          Format.asprintf "#define %-30s %a"
            (SymbolTable.name symtab name)
            ApiCodegen.cg_expr expr
        )
      in
      Decl_Macro macro

  | decl ->
      visit_decl v symtab decl


let v = { default with map_decl }


let transform (symtab, decls) =
  symtab, visit_decls v symtab decls
