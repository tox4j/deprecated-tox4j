open ApiAst

let clone_uname = function
  | UName name -> UName name

let clone_lname = function
  | LName name -> LName name

let clone_macro = function
  | Macro macro -> Macro macro


let clone_comment_fragment = function
  | Cmtf_Doc doc -> Cmtf_Doc doc
  | Cmtf_UName uname -> Cmtf_UName (clone_uname uname)
  | Cmtf_LName lname -> Cmtf_LName (clone_lname lname)
  | Cmtf_Break -> Cmtf_Break


let clone_comment = function
  | Cmt_None -> Cmt_None
  | Cmt_Doc frags -> Cmt_Doc (List.map clone_comment_fragment frags)
  | Cmt_Section frags -> Cmt_Section (List.map clone_comment_fragment frags)


let rec clone_size_spec = function
  | Ss_UName uname -> Ss_UName (clone_uname uname)
  | Ss_LName lname -> Ss_LName (clone_lname lname)
  | Ss_Size -> Ss_Size
  | Ss_Bounded (size_spec, uname) ->
      Ss_Bounded (clone_size_spec size_spec, clone_uname uname)


let rec clone_type_name = function
  | Ty_UName uname -> Ty_UName (clone_uname uname)
  | Ty_LName lname -> Ty_LName (clone_lname lname)
  | Ty_Array (lname, size_spec) ->
      Ty_Array (clone_lname lname, clone_size_spec size_spec)
  | Ty_This -> Ty_This
  | Ty_Const type_name -> Ty_Const (clone_type_name type_name)


let rec clone_enumerator = function
  | Enum_Name (comment, uname) ->
      Enum_Name (clone_comment comment, clone_uname uname)
  | Enum_Namespace (uname, enumerators) ->
      Enum_Namespace (clone_uname uname, List.map clone_enumerator enumerators)


let clone_error_list = function
  | Err_None -> Err_None
  | Err_From lname -> Err_From (clone_lname lname)
  | Err_List enumerators -> Err_List (List.map clone_enumerator enumerators)


let clone_parameter = function
  | Param (type_name, lname) ->
      Param (clone_type_name type_name, clone_lname lname)


let clone_function_name = function
  | Fn_Custom (type_name, lname) ->
      Fn_Custom (clone_type_name type_name, clone_lname lname)
  | Fn_Size -> Fn_Size
  | Fn_Get -> Fn_Get
  | Fn_Set -> Fn_Set


let rec clone_expr = function
  | E_Number num -> E_Number num
  | E_UName uname -> E_UName (clone_uname uname)
  | E_Sizeof lname -> E_Sizeof (clone_lname lname)
  | E_Plus (lhs, rhs) -> E_Plus (clone_expr lhs, clone_expr rhs)


let rec clone_decl = function
  | Decl_Comment (comment, decl) ->
      Decl_Comment (clone_comment comment, clone_decl decl)
  | Decl_Static decl -> Decl_Static (clone_decl decl)
  | Decl_Macro macro -> Decl_Macro (clone_macro macro)
  | Decl_Namespace (lname, decls) ->
      Decl_Namespace (clone_lname lname, clone_decls decls)
  | Decl_Class (lname, decls) ->
      Decl_Class (clone_lname lname, clone_decls decls)
  | Decl_Function (function_name, parameters, error_list) ->
      Decl_Function (
        clone_function_name function_name,
        List.map clone_parameter parameters,
        clone_error_list error_list
      )
  | Decl_Const (uname, expr) ->
      Decl_Const (clone_uname uname, clone_expr expr)
  | Decl_Enum (is_class, uname, enumerators) ->
      Decl_Enum (
        is_class,
        clone_uname uname,
        List.map clone_enumerator enumerators
      )
  | Decl_Error (lname, enumerators) ->
      Decl_Error (
        clone_lname lname,
        List.map clone_enumerator enumerators
      )
  | Decl_Struct decls -> Decl_Struct (clone_decls decls)
  | Decl_Member (type_name, lname) ->
      Decl_Member (
        clone_type_name type_name,
        clone_lname lname
      )
  | Decl_GetSet (type_name, lname, decls) ->
      Decl_GetSet (
        clone_type_name type_name,
        clone_lname lname,
        clone_decls decls
      )
  | Decl_Typedef (lname, parameters) ->
      Decl_Typedef (clone_lname lname, List.map clone_parameter parameters)
  | Decl_Event (lname, decl) ->
      Decl_Event (clone_lname lname, clone_decl decl)


and clone_decls decls = List.map clone_decl decls
