open ApiAst
open ApiFoldMap


let fold_decl v (repl, ignore_first) = function
  | Decl_Namespace (lname, decls) ->
      let (repl, _), decls =
        ReplaceDecl.fold_decls v (repl, ignore_first - 1) decls
      in

      let (repl, _) =
        if ignore_first > 0 then
          (repl, ignore_first)
        else
          ReplaceDecl.replace (repl, ignore_first) decls
      in
      (repl, ignore_first), Decl_Namespace (lname, decls)

  | decl ->
      ReplaceDecl.fold_decl v (repl, ignore_first) decl


let v = { default with fold_decl }


let transform ignore_first (symtab, decls) =
  let _, decls =
    ReplaceDecl.fold_decls v (ReplaceDecl.initial, ignore_first) decls
  in
  symtab, decls
