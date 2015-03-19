open ApiAst
open ApiFold


let fold_enumerator v (symtab, enum) = function
  | Enum_Name (comment, uname) ->
      SymbolTable.rename symtab uname
        (fun name -> enum ^ "_" ^ name), enum

  | enumerator ->
      visit_enumerator v (symtab, enum) enumerator


let fold_decl v (symtab, enum) = function
  | Decl_Enum (_, lname, enumerators) ->
      let symtab, _ =
        let enum = SymbolTable.name symtab lname in
        visit_list v.fold_enumerator v (symtab, enum) enumerators
      in
      symtab, enum

  | decl ->
      visit_decl v (symtab, enum) decl


let v = {
  default with
  fold_enumerator;
  fold_decl;
}


let transform (symtab, decls) =
  fst (visit_decls v (symtab, "") decls), decls
