open ApiAst
open ApiMap


let rec add_types name ty = function
  | Decl_Comment (comment, decl) ->
      let decl = add_types name ty decl in
      Decl_Comment (comment, decl)

  | Decl_Function (return_type, fname, parameters, error_list) as decl ->
      let return_type ty =
        match return_type with
        | Ty_Auto -> ty
        | ty -> ty
      in
      let size_t = TypeName.size_t in
      let void = TypeName.void in

      begin match fname with
        | "size" ->
            Decl_Function (return_type size_t, fname, parameters, error_list)

        | "get" ->
            if TypeName.is_array ty then
              let parameters = parameters @ [Param (ty, name)] in
              Decl_Function (return_type void, fname, parameters, error_list)

            else
              Decl_Function (return_type ty, fname, parameters, error_list)

        | "set" ->
            let ty =
              if TypeName.is_array ty then
                Ty_Const ty
              else
                ty
            in
            let parameters = parameters @ [Param (ty, name)] in
            Decl_Function (return_type void, fname, parameters, error_list)

        | _ -> failwith @@ show_decl Format.pp_print_string decl
      end

  | decl ->
      failwith @@ show_decl Format.pp_print_string decl


let map_decl v state = function
  | Decl_GetSet (type_name, lname, decls) ->
      let decls = List.rev_map (add_types lname type_name) decls in

      Decl_GetSet (type_name, lname, decls)

  | decl ->
      ApiMap.visit_decl v state decl


let v = { default with map_decl }


let transform decls =
  visit_decls v () decls
