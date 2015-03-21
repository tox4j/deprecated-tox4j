open ApiAst
open ApiFold


let fold_decl v (symtab, event) = function
  | Decl_Typedef (_, lname, _) ->
      SymbolTable.rename symtab lname
        (fun name -> event ^ "_" ^ name), event

  | Decl_Event (lname, decls) ->
      let symtab, _ =
        let event = SymbolTable.name symtab lname in
        visit_list v.fold_decl v (symtab, event) decls
      in
      symtab, event

  | decl ->
      visit_decl v (symtab, event) decl


let v = {
  default with
  fold_decl;
}


let transform (symtab, decls) =
  fst (visit_decls v (symtab, "") decls), decls
