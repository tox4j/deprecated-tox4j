open ApiAst
open ApiFoldMap


type 'id action =
  | Keep
  | Replace of 'id decl list
  | Prepend of 'id decl list
  | Append  of 'id decl list


type ('a, 'id) t = 'id action * 'a


let initial = Keep

let replace (_, user_state) decls =
  (Replace decls, user_state)

let prepend (_, user_state) decls =
  (Prepend decls, user_state)

let append (_, user_state) decls =
  (Append decls, user_state)

let set (replacement, _) user_state =
  (replacement, user_state)

let get = snd


let fold_decls v state decls =
  let state, decls =
    List.fold_left
      (fun (state, decls) decl ->
         let (replacement, user_state as state), decl =
           v.fold_decl v state decl
         in
         match replacement with
         | Keep ->
             state, decl :: decls
         | Replace replace ->
             (Keep, user_state), List.rev replace @ decls
         | Prepend prepend ->
             (Keep, user_state), decl :: List.rev prepend @ decls
         | _ -> failwith "Unhandled state in fold_decls"
      ) (state, []) decls
  in
  assert (fst state = Keep);
  state, List.rev decls


let fold_decl v state = function
  | Decl_Comment (comment, decl) ->
      let state, comment = v.fold_comment v state comment in
      let state, decl = v.fold_decl v state decl in
      let replacement =
        match fst state with
        | Keep | Replace [] | Prepend _ as replacement -> replacement
        | Replace (decl0 :: decls) ->
            Replace (Decl_Comment (comment, decl0) :: decls)
        | _ -> failwith "Unhandled state"
      in
      (replacement, snd state), Decl_Comment (comment, decl)
  | Decl_Namespace (lname, decls) ->
      let state, lname = v.fold_lname v state lname in
      let state, decls = fold_decls v state decls in
      state, Decl_Namespace (lname, decls)
  | Decl_Class (lname, decls) ->
      let state, lname = v.fold_lname v state lname in
      let state, decls = fold_decls v state decls in
      state, Decl_Class (lname, decls)
  | Decl_Struct (lname, decls) ->
      let state, lname = v.fold_lname v state lname in
      let state, decls = fold_decls v state decls in
      state, Decl_Struct (lname, decls)
  | Decl_GetSet (type_name, lname, decls) ->
      let state, type_name = v.fold_type_name v state type_name in
      let state, lname = v.fold_lname v state lname in
      let state, decls = fold_decls v state decls in
      state, Decl_GetSet (type_name, lname, decls)

  | decl ->
      visit_decl v state decl


let v = { default with fold_decl }
