open ApiAst
open ApiFoldMap


type ('a, 'id) t = {
  state : 'a;
  replacement : 'id decl list option;
}

let initial state = {
  state;
  replacement = None;
}

let replace state replacement = {
  state with replacement = Some replacement
}


let fold_decls v state decls =
  let state, decls =
    List.fold_left
      (fun (state, decls) decl ->
         let state, decl = v.fold_decl v state decl in
         match state.replacement with
         | None ->
             state, decl :: decls
         | Some replacement ->
             { state with replacement = None }, List.rev replacement @ decls
      ) (state, []) decls
  in
  assert (state.replacement = None);
  state, List.rev decls


let fold_decl v state = function
  | Decl_Comment (comment, decl) ->
      let state, comment = v.fold_comment v state comment in
      let state, decl = v.fold_decl v state decl in
      let state = {
        state with
        replacement =
          match state.replacement with
          | None -> None
          | Some [] -> Some []
          | Some (decl0 :: decls) ->
              Some (Decl_Comment (comment, decl0) :: decls)
      } in
      state, Decl_Comment (comment, decl)
  | Decl_Namespace (lname, decls) ->
      let state, lname = v.fold_lname v state lname in
      let state, decls = fold_decls v state decls in
      state, Decl_Namespace (lname, decls)
  | Decl_Class (lname, decls) ->
      let state, lname = v.fold_lname v state lname in
      let state, decls = fold_decls v state decls in
      state, Decl_Class (lname, decls)
  | Decl_Struct decls ->
      let state, decls = fold_decls v state decls in
      state, Decl_Struct decls
  | Decl_GetSet (type_name, lname, decls) ->
      let state, type_name = v.fold_type_name v state type_name in
      let state, lname = v.fold_lname v state lname in
      let state, decls = fold_decls v state decls in
      state, Decl_GetSet (type_name, lname, decls)

  | decl ->
      visit_decl v state decl


let v = { default with fold_decl }
