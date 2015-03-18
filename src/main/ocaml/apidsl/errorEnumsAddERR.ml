open ApiAst
open ApiFold


let fold_decl v symtab = function
  | Decl_Error (lname, enumerators) ->
      SymbolTable.rename symtab lname
        (fun name -> "ERR_" ^ name)

  | decl ->
      visit_decl v symtab decl


let v = { default with fold_decl }


let transform (symtab, decls) =
  visit_decls v symtab decls, decls
