open ApiAst


let c_mode = true


let cg_list ?(sep="") cg fmt l =
  ignore (
    List.fold_left
      (fun first x ->
         if not first then
           Format.pp_print_string fmt sep;
         Format.fprintf fmt "%a" cg x;
         false
      ) true l
  )


let cg_braced ?(after_brace="") cg fmt x =
  let after_brace =
    if c_mode then
      after_brace ^ ";"
    else
      after_brace
  in
  Format.fprintf fmt "{@,@[<v2>%a@]@,}%s@," cg x after_brace


let cg_uname = Format.pp_print_string
let cg_lname = Format.pp_print_string
let cg_macro fmt (Macro s) = Format.pp_print_string fmt s


let cg_var fmt = function
  | Var_UName uname ->
      Format.fprintf fmt "%a"
        cg_uname uname
  | Var_LName lname ->
      Format.fprintf fmt "%a"
        cg_lname lname
  | Var_Event ->
      Format.fprintf fmt "event"


let cg_comment_fragment fmt = function
  | Cmtf_Doc doc ->
      Format.fprintf fmt "%s" doc
  | Cmtf_UName name ->
      if c_mode then
        cg_uname fmt name
      else
        Format.fprintf fmt "${%a}"
          cg_uname name
  | Cmtf_LName name ->
      if c_mode then
        cg_lname fmt name
      else
        Format.fprintf fmt "${%a}"
          cg_lname name
  | Cmtf_Var var ->
      if c_mode then
        cg_list ~sep:"." cg_var fmt var
      else
        Format.fprintf fmt "${%a}"
          (cg_list ~sep:"." cg_var) var
  | Cmtf_Break ->
      Format.fprintf fmt "@, *"


let cg_comment fmt = function
  | Cmt_None ->
      Format.fprintf fmt "@,/**%a@, */"
        (cg_list cg_comment_fragment) [Cmtf_Break; Cmtf_Doc " TODO: Generate doc"]
  | Cmt_Doc frags ->
      Format.fprintf fmt "@,/**%a@, */"
        (cg_list cg_comment_fragment) frags
;;


let rec cg_size_spec fmt = function
  | Ss_UName uname ->
      Format.fprintf fmt "%a"
        cg_uname uname
  | Ss_LName lname ->
      Format.fprintf fmt "%a"
        cg_lname lname
  | Ss_Bounded (lhs, rhs) ->
      Format.fprintf fmt "%a <= %a"
        cg_lname lhs
        cg_uname rhs


let cg_size_spec fmt spec =
  Format.fprintf fmt "[%a]" cg_size_spec spec


let rec cg_type_name fmt = function
  | Ty_UName uname ->
      Format.fprintf fmt "%a "
        cg_uname uname
  | Ty_LName lname ->
      if String.contains lname '_' &&
         Char.uppercase lname.[0] = lname.[0] then
        Format.fprintf fmt "struct ";
      Format.fprintf fmt "%a "
        cg_lname lname
  | Ty_Array (lname, size_spec) ->
      Format.fprintf fmt "%a%a "
        cg_lname lname
        cg_size_spec size_spec
  | Ty_Auto ->
      Format.fprintf fmt "auto "
  | Ty_Const type_name ->
      Format.fprintf fmt "const %a"
        cg_type_name type_name
  | Ty_Pointer type_name ->
      Format.fprintf fmt "%a*"
        cg_type_name type_name


let cg_function_name fmt = function
  | (Ty_Auto, name) ->
      Format.fprintf fmt "%a"
        cg_lname name
  | (type_name, name) ->
      Format.fprintf fmt "%a%a"
        cg_type_name type_name
        cg_lname name


let rec cg_enumerator fmt = function
  | Enum_Name (comment, uname) ->
      Format.fprintf fmt "%a@,%a,@,"
        cg_comment comment
        cg_uname uname
  | Enum_Namespace (uname, enums) ->
      Format.fprintf fmt "@,namespace %a %a"
        cg_uname uname
        (cg_braced cg_enumerators) enums


and cg_enumerators fmt enums =
  cg_list cg_enumerator fmt enums


let cg_error_list fmt = function
  | Err_None ->
      Format.fprintf fmt ";"
  | Err_From (name) ->
      Format.fprintf fmt "@,    with error for %a;"
        cg_lname name
  | Err_List enums ->
      Format.fprintf fmt " %a"
        (cg_braced cg_enumerators) enums


let cg_parameter fmt = function
  | Param (type_name, lname) ->
      Format.fprintf fmt "%a%a"
        cg_type_name type_name
        cg_lname lname


let cg_parameters fmt params =
  Format.fprintf fmt "(%a)"
    (cg_list ~sep:", " cg_parameter) params


let rec cg_expr fmt = function
  | E_Number i ->
      Format.fprintf fmt "%d" i
  | E_UName uname ->
      Format.fprintf fmt "%a"
        cg_uname uname
  | E_Sizeof lname ->
      Format.fprintf fmt "sizeof(%a)"
        cg_lname lname
  | E_Plus (lhs, rhs) ->
      Format.fprintf fmt "%a + %a"
        cg_expr lhs
        cg_expr rhs


let cg_expr fmt expr =
  let is_simple =
    match expr with
    | E_Number _ | E_UName _ -> true
    | E_Sizeof _ | E_Plus _ -> false
  in
  if not is_simple then
    Format.pp_print_char fmt '(';
  cg_expr fmt expr;
  if not is_simple then
    Format.pp_print_char fmt ')';
;;


let rec cg_decl_qualified qualifier fmt = function
  | Decl_Comment (comment, decl) ->
      assert (qualifier = "");
      Format.fprintf fmt "%a%a"
        cg_comment comment
        cg_decl decl
  | Decl_Macro (macro) ->
      assert (qualifier = "");
      Format.fprintf fmt "@,%a"
        cg_macro macro
  | Decl_Namespace (name, decls) ->
      Format.fprintf fmt "@,%snamespace %a %a"
        qualifier
        cg_lname name
        (cg_braced cg_decls) decls
  | Decl_Class (lname, []) ->
      Format.fprintf fmt "@,%sclass %a;"
        qualifier
        cg_lname lname
  | Decl_Class (lname, decls) ->
      Format.fprintf fmt "@,%sclass %a %a"
        qualifier
        cg_lname lname
        (cg_braced cg_decls) decls
  | Decl_Function (type_name, lname, parameters, error_list) ->
      Format.fprintf fmt "@,%s%a%a%a"
        qualifier
        cg_function_name (type_name, lname)
        cg_parameters parameters
        cg_error_list error_list
  | Decl_Const (uname, expr) ->
      assert (qualifier = "");
      Format.fprintf fmt "@,const %a = %a;"
        cg_uname uname
        cg_expr expr
  | Decl_Enum (is_class, uname, enumerators) ->
      assert (qualifier = "");
      let after_brace =
        if is_class && c_mode then
          Some (" " ^ uname)
        else
          None
      in
      Format.fprintf fmt "@,%senum%s %a %a"
        (if is_class &&     c_mode then "typedef " else "")
        (if is_class && not c_mode then " class" else "")
        cg_uname uname
        (cg_braced ?after_brace cg_enumerators) enumerators
  | Decl_Error (lname, enumerators) ->
      assert (qualifier = "");
      Format.fprintf fmt "@,error for %a %a"
        cg_lname lname
        (cg_braced cg_enumerators) enumerators
  | Decl_Struct (lname, []) ->
      assert (qualifier = "");
      let uname = String.uppercase lname in
      if c_mode then (
        Format.fprintf fmt "@,#ifndef %a_DEFINED"
          cg_uname uname;
        Format.fprintf fmt "@,#define %a_DEFINED"
          cg_uname uname;
      );
      Format.fprintf fmt "@,%sstruct %a%s;"
        (if c_mode then "typedef " else "")
        cg_lname lname
        (if c_mode then " " ^ lname else "")
      ;
      if c_mode then (
        Format.fprintf fmt "@,#endif /* %a_DEFINED */"
          cg_uname uname;
      );
  | Decl_Struct (lname, decls) ->
      assert (qualifier = "");
      Format.fprintf fmt "@,struct %a %a"
        cg_lname lname
        (cg_braced cg_decls) decls
  | Decl_Member (type_name, lname) ->
      assert (qualifier = "");
      Format.fprintf fmt "@,%a%a;@,"
        cg_type_name type_name
        cg_lname lname
  | Decl_GetSet (type_name, lname, decls) ->
      assert (qualifier = "");
      Format.fprintf fmt "@,%a%a %a"
        cg_type_name type_name
        cg_lname lname
        (cg_braced cg_decls) decls
  | Decl_Typedef (type_name, lname, parameters) ->
      assert (qualifier = "");
      Format.fprintf fmt "@,typedef %a%a%a;@,"
        cg_type_name type_name
        cg_lname lname
        cg_parameters parameters
  | Decl_Event (lname, decl) ->
      assert (qualifier = "");
      Format.fprintf fmt "@,event %a %a"
        cg_lname lname
        (cg_braced cg_decls) decl
  | Decl_Inline (decl) ->
      assert (qualifier = "");
      Format.fprintf fmt "%a"
        (cg_decl_qualified "inline ") decl
  | Decl_Static (decl) ->
      assert (qualifier = "");
      Format.fprintf fmt "%a"
        (cg_decl_qualified "static ") decl
  | Decl_Section frags ->
      Format.fprintf fmt "@,@,/*";
      for i = 0 to 77 do
        Format.pp_print_char fmt '*'
      done;
      Format.fprintf fmt "%a"
        (cg_list cg_comment_fragment) frags;
      Format.fprintf fmt "@, ";
      for i = 0 to 77 do
        Format.pp_print_char fmt '*'
      done;
      Format.fprintf fmt "/@,@,";


and cg_decl fmt decl =
  cg_decl_qualified "" fmt decl


and cg_decls fmt = cg_list ~sep:"\n" cg_decl fmt


let cg_decls fmt decls =
  Format.fprintf fmt "@[<v>%a@]"
    cg_decls decls
