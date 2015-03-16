open ApiAst


let void = Ty_LName (LName "void")
let size_t = Ty_LName (LName "size_t")


let rec is_array = function
  | Ty_Const ty -> is_array ty
  | Ty_Array _ -> true
  | _ -> false


let rec is_var_array = function
  | Ty_Const ty -> is_var_array ty
  | Ty_Array (_, Ss_UName _) -> false
  | Ty_Array _ -> true
  | _ -> false


let rec length_param_of_size_spec = function
  | Ss_UName n ->
      failwith (
        "UName as parameter name: " ^ show_uname Format.pp_print_string n
      )
  | Ss_LName n -> n
  | Ss_Bounded (spec, _) -> length_param_of_size_spec spec


let rec length_param = function
  | Ty_Const ty -> length_param ty
  | Ty_Array (_, size_spec) ->
      length_param_of_size_spec size_spec

  | ty ->
      failwith (
        "Not an array type: " ^ show_type_name Format.pp_print_string ty
      )
