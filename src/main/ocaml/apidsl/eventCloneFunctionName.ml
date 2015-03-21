open ApiAst
open ApiFoldMap


let fold_decl v (symtab, in_event) = function

  | Decl_Function (type_name, lname, parameters, error_list) when in_event ->
      let symtab, lname = SymbolTable.clone_symbol symtab lname in

      let symtab =
        SymbolTable.rename symtab lname
          (fun name -> "callback_" ^ name)
      in

      (symtab, in_event), Decl_Function (type_name, lname, parameters, error_list)

  | Decl_Event (lname, decls) ->
      let (symtab, _), decls = visit_decls v (symtab, true) decls in
      (symtab, false), Decl_Event (lname, decls)

  | decl ->
      visit_decl v (symtab, in_event) decl


let v = { default with fold_decl }


let transform (symtab, decls) =
  let (symtab, _), decls = visit_decls v (symtab, false) decls in
  symtab, decls
