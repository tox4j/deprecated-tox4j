open ApiAst
open ApiFoldMap


let fold_decl v state = function
  | Decl_GetSet (type_name, lname, decls) ->
      let state, decls = ReplaceDecl.fold_decls v state decls in

      let state = ReplaceDecl.replace state decls in

      state, Decl_GetSet (type_name, lname, decls)

  | decl ->
      ReplaceDecl.fold_decl v state decl


let v = { default with fold_decl }


let transform (symtab, decls) =
  let state, decls =
    ReplaceDecl.fold_decls v (ReplaceDecl.initial, ()) decls
  in
  symtab, decls
