open ApiAst


let translate_get ty name parameters error_list =
  let fn_name = LName.prepend "get" name in

  if TypeName.is_array ty then
    let parameters =
      if TypeName.is_var_array ty then
        Param (TypeName.size_t, TypeName.length_param ty) :: parameters
      else
        parameters
    in
    let parameters = Param (ty, name) :: parameters in
    Decl_Function (TypeName.void, fn_name, parameters, error_list)

  else
    Decl_Function (ty, fn_name, parameters, error_list)


let translate_set ty name parameters error_list =
  let fn_name = LName.prepend "set" name in
  let parameters =
    if TypeName.is_var_array ty then
      Param (TypeName.size_t, TypeName.length_param ty) :: parameters
    else
      parameters
  in
  let parameters = Param (ty, name) :: parameters in
  Decl_Function (TypeName.void, fn_name, parameters, error_list)


let rec translate_get_set ty name = function
  | Decl_Comment (comment, decl) ->
      let decl = translate_get_set ty name decl in
      Decl_Comment (comment, decl)

  | Decl_Function (Ty_Auto, fname, parameters, error_list) as decl ->
      begin match LName.to_string fname with
        | "size" ->
            let name = LName.prepend "get" name in
            let name = LName.append name "size" in
            Decl_Function (TypeName.size_t, name, parameters, error_list)
        | "get" ->
            translate_get ty name parameters error_list
        | "set" ->
            translate_set ty name parameters error_list
        | _ ->
            decl
      end

  | decl ->
      failwith @@ show_decl Format.pp_print_string decl


let translate_get_set ty name decls =
  List.map (translate_get_set ty name) decls


let transform decls =
  let open ApiFoldMap in

  let fold_decl v state = function
    | Decl_GetSet (type_name, lname, decls) as decl ->
        let state =
          ReplaceDecl.replace state (translate_get_set type_name lname decls)
        in
        state, decl

    | decl ->
        ReplaceDecl.fold_decl v state decl
  in

  let v = { default with fold_decl } in
  snd @@ visit_decls v (ReplaceDecl.initial ()) decls
