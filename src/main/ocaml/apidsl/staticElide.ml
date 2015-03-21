open ApiAst
open ApiMap


let map_decl v symtab = function

  | Decl_Static decl ->
      decl

  | decl ->
      visit_decl v symtab decl


let v = { default with map_decl }


let transform (symtab, decls) =
  symtab, visit_decls v symtab decls
