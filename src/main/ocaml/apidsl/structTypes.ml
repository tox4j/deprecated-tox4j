open ApiAst
open ApiMap


let map_decl v symtab = function

  | Decl_Static (Decl_Class (name, decls))
  | Decl_Class (name, decls) ->
      let decls = visit_list v.map_decl v symtab decls in
      Decl_Namespace (name, decls)


  | decl ->
      visit_decl v symtab decl


let v = { default with map_decl }


let transform (symtab, decls) =
  symtab, visit_decls v symtab decls
