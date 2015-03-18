open ApiAst
open ApiFold


let fold_decl v symtab = function
  | Decl_Error (lname, enumerators) ->
      SymbolTable.rename symtab lname
        (fun name ->
           assert (String.sub name 0 6 = "error ");
           String.uppercase (String.sub name 6 (String.length name - 6))
        )

  | decl ->
      visit_decl v symtab decl


let v = { default with fold_decl }


let transform (symtab, decls) =
  visit_decls v symtab decls, decls
