open ApiAst
open ApiFoldMap


let fold_decl v state = function
  | Decl_Function (type_name, lname, parameters, Err_List enumerators) ->
      let state =
        ReplaceDecl.prepend state [Decl_Error (lname, enumerators)]
      in
      state, Decl_Function (type_name, lname, parameters, Err_From lname)

  | decl ->
      ReplaceDecl.fold_decl v state decl


let v = { default with fold_decl }


let transform decls =
  snd @@ ReplaceDecl.fold_decls v (ReplaceDecl.initial, ()) decls
