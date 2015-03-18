open ApiAst
open ApiMap


let map_decl v state = function
  | Decl_Error (lname, enumerators) ->
      Decl_Enum (true, lname, enumerators)

  | decl ->
      visit_decl v state decl


let v = { default with map_decl }


let transform (symtab, decls) =
  symtab, visit_decls v () decls
