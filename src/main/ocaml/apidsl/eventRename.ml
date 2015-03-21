open ApiAst
open ApiFold


let fold_decl v symtab = function
  | Decl_Event (lname, _) ->
      SymbolTable.rename symtab lname
        (fun name ->
           assert (String.sub name 0 6 = "event ");
           String.sub name 6 (String.length name - 6)
        )

  | decl ->
      visit_decl v symtab decl


let v = { default with fold_decl }


let transform (symtab, decls) =
  visit_decls v symtab decls, decls
