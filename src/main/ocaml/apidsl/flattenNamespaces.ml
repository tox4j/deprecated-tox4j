open ApiAst
open ApiFoldMap


let fold_decl v state = function
  | Decl_Namespace (lname, decls) ->
      let state, decls = ReplaceDecl.fold_decls v state decls in

      let state = ReplaceDecl.replace state decls in
      state, Decl_Namespace (lname, decls)

  | decl ->
      ReplaceDecl.fold_decl v state decl


let v = { default with fold_decl }


let transform (symtab, decls) =
  symtab, snd @@ ReplaceDecl.fold_decls v (ReplaceDecl.initial ()) decls
