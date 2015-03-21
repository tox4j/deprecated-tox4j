open ApiAst
open ApiMap


let map_decl v this_name = function

  | Decl_Static (Decl_Namespace _)
  | Decl_Static (Decl_Function _) as decl ->
      decl

  | Decl_Function (Ty_Const (type_name), lname, parameters, error_list) ->
      let this_type = Ty_Const (Ty_Pointer TypeName.this) in
      let parameters = Param (this_type, this_name) :: parameters in
      Decl_Function (type_name, lname, parameters, error_list)

  | Decl_Function (type_name, lname, parameters, error_list) ->
      let parameters =
        let this_type =
          match lname with
          | "get" | "size" ->
              Ty_Const (Ty_Pointer TypeName.this)
          | _ ->
              Ty_Pointer TypeName.this
        in
        Param (this_type, this_name) :: parameters
      in
      Decl_Function (type_name, lname, parameters, error_list)

  | Decl_Typedef (type_name, lname, parameters) ->
      let this_type = Ty_Pointer TypeName.this in
      let parameters = Param (this_type, this_name) :: parameters in
      Decl_Typedef (type_name, lname, parameters)

  | Decl_Class (lname, decls) ->
      let decls = visit_list v.map_decl v lname decls in
      Decl_Class (lname, decls)

  | decl ->
      visit_decl v this_name decl


let v = { default with map_decl }


let transform decls =
  visit_decls v "" decls
