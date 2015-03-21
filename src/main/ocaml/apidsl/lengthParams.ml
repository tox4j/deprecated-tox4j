open ApiAst
open ApiMap


let add_length_parameters lname parameters =
  if lname = "get" then
    parameters
  else
    List.rev (
      List.fold_left
        (fun parameters -> function
           | Param (ty, name) as param when TypeName.is_var_array ty ->
               let length = TypeName.length_param ty in
               Param (TypeName.size_t, length) :: param :: parameters

           | param -> param :: parameters
        ) [] parameters
    )


let map_decl v state = function

  | Decl_Typedef (type_name, lname, parameters) ->
      let type_name = v.map_type_name v state type_name in
      let lname = v.map_lname v state lname in
      let parameters = add_length_parameters lname parameters in
      Decl_Typedef (type_name, lname, parameters)

  | Decl_Function (type_name, lname, parameters, error_list) ->
      let type_name = v.map_type_name v state type_name in
      let lname = v.map_lname v state lname in
      let parameters = add_length_parameters lname parameters in
      let error_list = v.map_error_list v state error_list in
      Decl_Function (type_name, lname, parameters, error_list)

  | decl ->
      visit_decl v state decl


let v = { default with map_decl }


let transform decls =
  visit_decls v () decls
