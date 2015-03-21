open ApiAst
open ApiFoldMap


let can_apply_static = function
  | Decl_Macro _
  | Decl_Class _
  | Decl_Function _
  | Decl_GetSet _
  | Decl_Namespace _ ->
      true
  | Decl_Member _
  | Decl_Typedef _
  | Decl_Event _
  | Decl_Const _
  | Decl_Enum _
  | Decl_Error _
  | Decl_Struct _
  | Decl_Comment _
  | Decl_Section _
  | Decl_Inline _
  | Decl_Static _ ->
      false


let fold_decl v static decl =
  let _, decl =
    match decl with
    | Decl_Static (Decl_Namespace (name, decls)) ->
        (* Add static to the inner scope. *)
        let static' = true in
        let static', decls = visit_list v.fold_decl v static' decls in
        assert (static');
        static', Decl_Namespace (name, decls)

    | decl ->
        let static =
          if can_apply_static decl then
            false
          else
            static
        in
        visit_decl v static decl
  in

  (* Attach static to everything in this scope, if the outer scope was
     static. *)
  if static && can_apply_static decl then
    static, Decl_Static decl
  else
    static, decl


let v = { default with fold_decl }


let transform (symtab, decls) =
  symtab, snd @@ visit_decls v false decls
