open ApiAst


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


let cg_braced cg fmt x =
  Format.fprintf fmt "{@,@[<v2>%a@]@,}@," cg x


let cg_uname fmt (UName s) = Format.pp_print_string fmt s
let cg_lname fmt (LName s) = Format.pp_print_string fmt s
let cg_macro fmt (Macro s) = Format.pp_print_string fmt s


let cg_comment_fragment fmt = function
  | Cmtf_Doc doc ->
      Format.fprintf fmt "%s" doc
  | Cmtf_UName name ->
      Format.fprintf fmt "${%a}"
        cg_uname name
  | Cmtf_LName name ->
      Format.fprintf fmt "${%a}"
        cg_lname name
  | Cmtf_Break ->
      Format.fprintf fmt "@, *"


let cg_comment fmt = function
  | Cmt_None ->
      Format.fprintf fmt "@,/**%a@, */"
        (cg_list cg_comment_fragment) [Cmtf_Break; Cmtf_Doc " TODO: Generate doc"]
  | Cmt_Doc frags ->
      Format.fprintf fmt "@,/**%a@, */"
        (cg_list cg_comment_fragment) frags
  | Cmt_Section frags ->
      Format.fprintf fmt "@,/*";
      for i = 0 to 77 do
        Format.pp_print_char fmt '*'
      done;
      Format.fprintf fmt "%a"
        (cg_list cg_comment_fragment) frags;
      Format.fprintf fmt "@, ";
      for i = 0 to 77 do
        Format.pp_print_char fmt '*'
      done;
      Format.fprintf fmt "/@,@,@,";
;;


let rec cg_size_spec fmt = function
  | Ss_UName uname ->
      Format.fprintf fmt "%a"
        cg_uname uname
  | Ss_LName lname ->
      Format.fprintf fmt "%a"
        cg_lname lname
  | Ss_Size ->
      Format.fprintf fmt "size"
  | Ss_Bounded (lhs, rhs) ->
      Format.fprintf fmt "%a <= %a"
        cg_size_spec lhs
        cg_uname rhs


let cg_size_spec fmt spec =
  Format.fprintf fmt "[%a]" cg_size_spec spec


let rec cg_type_name fmt = function
  | Ty_UName uname ->
      Format.fprintf fmt "%a"
        cg_uname uname
  | Ty_LName lname ->
      Format.fprintf fmt "%a"
        cg_lname lname
  | Ty_Array (lname, size_spec) ->
      Format.fprintf fmt "%a%a"
        cg_lname lname
        cg_size_spec size_spec
  | Ty_This ->
      Format.fprintf fmt "this"
  | Ty_Const type_name ->
      Format.fprintf fmt "const %a"
        cg_type_name type_name


let cg_function_name fmt = function
  | Fn_Custom (type_name, name) ->
      Format.fprintf fmt "%a %a"
        cg_type_name type_name
        cg_lname name
  | Fn_Size ->
      Format.fprintf fmt "size"
  | Fn_Get ->
      Format.fprintf fmt "get"
  | Fn_Set ->
      Format.fprintf fmt "set"


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
      Format.fprintf fmt "%a %a"
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
  | Decl_Function (function_name, parameters, error_list) ->
      Format.fprintf fmt "@,%s%a%a%a"
        qualifier
        cg_function_name function_name
        cg_parameters parameters
        cg_error_list error_list
  | Decl_Const (uname, expr) ->
      assert (qualifier = "");
      Format.fprintf fmt "@,const %a = %a;"
        cg_uname uname
        cg_expr expr
  | Decl_Enum (is_class, uname, enumerators) ->
      assert (qualifier = "");
      Format.fprintf fmt "@,enum%s %a %a"
        (if is_class then " class" else "")
        cg_uname uname
        (cg_braced cg_enumerators) enumerators
  | Decl_Error (lname, enumerators) ->
      assert (qualifier = "");
      Format.fprintf fmt "@,error for %a %a"
        cg_lname lname
        (cg_braced cg_enumerators) enumerators
  | Decl_Struct (decls) ->
      assert (qualifier = "");
      Format.fprintf fmt "@,struct this %a"
        (cg_braced cg_decls) decls
  | Decl_Member (type_name, lname) ->
      assert (qualifier = "");
      Format.fprintf fmt "@,%a %a;@,"
        cg_type_name type_name
        cg_lname lname
  | Decl_GetSet (type_name, lname, decls) ->
      assert (qualifier = "");
      Format.fprintf fmt "@,%a %a %a"
        cg_type_name type_name
        cg_lname lname
        (cg_braced cg_decls) decls
  | Decl_Typedef (lname, parameters) ->
      assert (qualifier = "");
      Format.fprintf fmt "@,typedef %a%a;@,"
        cg_lname lname
        cg_parameters parameters
  | Decl_Event (lname, decl) ->
      assert (qualifier = "");
      Format.fprintf fmt "@,event %a %a"
        cg_lname lname
        (cg_braced cg_decl) decl
  | Decl_Static (decl) ->
      assert (qualifier = "");
      Format.fprintf fmt "%a"
        (cg_decl_qualified "static ") decl


and cg_decl fmt decl =
  cg_decl_qualified "" fmt decl


and cg_decls fmt = cg_list ~sep:"\n" cg_decl fmt


let cg_decls fmt decls =
  Format.fprintf fmt "@[<v>%a@]"
    cg_decls decls
