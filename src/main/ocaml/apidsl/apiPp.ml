open ApiAst


let pr_list ?(sep="") pp fmt l =
  ignore (
    List.fold_left
      (fun first x ->
         if not first then
           Format.pp_print_string fmt sep;
         Format.fprintf fmt "%a" pp x;
         false
      ) true l
  )


let pr_braced pp fmt x =
  Format.fprintf fmt "{@,@[<v2>%a@]@,}@," pp x


let pr_uname fmt (UName (_, s)) = Format.pp_print_string fmt s
let pr_lname fmt (LName (_, s)) = Format.pp_print_string fmt s
let pr_macro fmt (Macro s) = Format.pp_print_string fmt s


let pr_comment_fragment fmt = function
  | Cmtf_Doc doc ->
      Format.fprintf fmt "%s" doc
  | Cmtf_UName name ->
      Format.fprintf fmt "${%a}"
        pr_uname name
  | Cmtf_LName name ->
      Format.fprintf fmt "${%a}"
        pr_lname name
  | Cmtf_Break ->
      Format.fprintf fmt "@, *"


let pr_comment fmt = function
  | Cmt_None ->
      Format.fprintf fmt "@,/**%a@, */"
        (pr_list pr_comment_fragment) [Cmtf_Break; Cmtf_Doc " TODO: Generate doc"]
  | Cmt_Doc frags ->
      Format.fprintf fmt "@,/**%a@, */"
        (pr_list pr_comment_fragment) frags
  | Cmt_Section frags ->
      Format.fprintf fmt "@,/*";
      for i = 0 to 77 do
        Format.pp_print_char fmt '*'
      done;
      Format.fprintf fmt "%a"
        (pr_list pr_comment_fragment) frags;
      Format.fprintf fmt "@, ";
      for i = 0 to 77 do
        Format.pp_print_char fmt '*'
      done;
      Format.fprintf fmt "/@,@,@,";
;;


let rec pr_size_spec fmt = function
  | Ss_UName uname ->
      Format.fprintf fmt "%a"
        pr_uname uname
  | Ss_LName lname ->
      Format.fprintf fmt "%a"
        pr_lname lname
  | Ss_Size ->
      Format.fprintf fmt "size"
  | Ss_Bounded (lhs, rhs) ->
      Format.fprintf fmt "%a <= %a"
        pr_size_spec lhs
        pr_uname rhs


let pr_size_spec fmt spec =
  Format.fprintf fmt "[%a]" pr_size_spec spec


let rec pr_type_name fmt = function
  | Ty_UName uname ->
      Format.fprintf fmt "%a"
        pr_uname uname
  | Ty_LName lname ->
      Format.fprintf fmt "%a"
        pr_lname lname
  | Ty_Array (lname, size_spec) ->
      Format.fprintf fmt "%a%a"
        pr_lname lname
        pr_size_spec size_spec
  | Ty_This ->
      Format.fprintf fmt "this"
  | Ty_Const type_name ->
      Format.fprintf fmt "const %a"
        pr_type_name type_name


let pr_function_name fmt = function
  | Fn_Custom (type_name, name) ->
      Format.fprintf fmt "%a %a"
        pr_type_name type_name
        pr_lname name
  | Fn_Size ->
      Format.fprintf fmt "size"
  | Fn_Get ->
      Format.fprintf fmt "get"
  | Fn_Set ->
      Format.fprintf fmt "set"


let rec pr_enumerator fmt = function
  | Enum_Name (comment, uname) ->
      Format.fprintf fmt "%a@,%a,@,"
        pr_comment comment
        pr_uname uname
  | Enum_Namespace (uname, enums) ->
      Format.fprintf fmt "@,namespace %a %a"
        pr_uname uname
        (pr_braced pr_enumerators) enums


and pr_enumerators fmt enums =
  pr_list pr_enumerator fmt enums


let pr_error_list fmt = function
  | Err_None ->
      Format.fprintf fmt ";"
  | Err_From (name) ->
      Format.fprintf fmt "@,    with error for %a;"
        pr_lname name
  | Err_List enums ->
      Format.fprintf fmt " %a"
        (pr_braced pr_enumerators) enums


let pr_parameter fmt = function
  | Param (type_name, lname) ->
      Format.fprintf fmt "%a %a"
        pr_type_name type_name
        pr_lname lname


let pr_parameters fmt params =
  Format.fprintf fmt "(%a)"
    (pr_list ~sep:", " pr_parameter) params


let rec pr_expr fmt = function
  | E_Number i ->
      Format.fprintf fmt "%d" i
  | E_UName uname ->
      Format.fprintf fmt "%a"
        pr_uname uname
  | E_Sizeof lname ->
      Format.fprintf fmt "sizeof(%a)"
        pr_lname lname
  | E_Plus (lhs, rhs) ->
      Format.fprintf fmt "%a + %a"
        pr_expr lhs
        pr_expr rhs


let rec pr_decl_qualified qualifier fmt = function
  | Decl_Comment (comment, decl) ->
      assert (qualifier = "");
      Format.fprintf fmt "%a%a"
        pr_comment comment
        pr_decl decl
  | Decl_Macro (macro) ->
      assert (qualifier = "");
      Format.fprintf fmt "@,%a"
        pr_macro macro
  | Decl_Namespace (name, decls) ->
      Format.fprintf fmt "@,%snamespace %a %a"
        qualifier
        pr_lname name
        (pr_braced pr_decls) decls
  | Decl_Class (lname, []) ->
      Format.fprintf fmt "@,%sclass %a;"
        qualifier
        pr_lname lname
  | Decl_Class (lname, decls) ->
      Format.fprintf fmt "@,%sclass %a %a"
        qualifier
        pr_lname lname
        (pr_braced pr_decls) decls
  | Decl_Function (function_name, parameters, error_list) ->
      Format.fprintf fmt "@,%s%a%a%a"
        qualifier
        pr_function_name function_name
        pr_parameters parameters
        pr_error_list error_list
  | Decl_Const (uname, expr) ->
      assert (qualifier = "");
      Format.fprintf fmt "@,const %a = %a;"
        pr_uname uname
        pr_expr expr
  | Decl_Enum (is_class, uname, enumerators) ->
      assert (qualifier = "");
      Format.fprintf fmt "@,enum%s %a %a"
        (if is_class then " class" else "")
        pr_uname uname
        (pr_braced pr_enumerators) enumerators
  | Decl_Error (lname, enumerators) ->
      assert (qualifier = "");
      Format.fprintf fmt "@,error for %a %a"
        pr_lname lname
        (pr_braced pr_enumerators) enumerators
  | Decl_Struct (decls) ->
      assert (qualifier = "");
      Format.fprintf fmt "@,struct this %a"
        (pr_braced pr_decls) decls
  | Decl_Member (type_name, lname) ->
      assert (qualifier = "");
      Format.fprintf fmt "@,%a %a;@,"
        pr_type_name type_name
        pr_lname lname
  | Decl_GetSet (type_name, lname, decls) ->
      assert (qualifier = "");
      Format.fprintf fmt "@,%a %a %a"
        pr_type_name type_name
        pr_lname lname
        (pr_braced pr_decls) decls
  | Decl_Typedef (lname, parameters) ->
      assert (qualifier = "");
      Format.fprintf fmt "@,typedef %a%a;@,"
        pr_lname lname
        pr_parameters parameters
  | Decl_Event (lname, decl) ->
      assert (qualifier = "");
      Format.fprintf fmt "@,event %a %a"
        pr_lname lname
        (pr_braced pr_decl) decl
  | Decl_Static (decl) ->
      assert (qualifier = "");
      Format.fprintf fmt "%a"
        (pr_decl_qualified "static ") decl


and pr_decl fmt decl =
  pr_decl_qualified "" fmt decl


and pr_decls fmt = pr_list ~sep:"\n" pr_decl fmt


let pp_decls fmt decls =
  Format.fprintf fmt "@[<v>%a@]"
    pr_decls decls
