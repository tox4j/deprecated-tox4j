open ApiAst


type ('a, 'id1, 'id2) t = {
  map_uname             : ('a, 'id1, 'id2) t -> 'a -> 'id1 uname -> 'id2 uname;
  map_lname             : ('a, 'id1, 'id2) t -> 'a -> 'id1 lname -> 'id2 lname;
  map_macro             : ('a, 'id1, 'id2) t -> 'a -> macro -> macro;
  map_var               : ('a, 'id1, 'id2) t -> 'a -> 'id1 var -> 'id2 var;
  map_comment_fragment  : ('a, 'id1, 'id2) t -> 'a -> 'id1 comment_fragment -> 'id2 comment_fragment;
  map_comment           : ('a, 'id1, 'id2) t -> 'a -> 'id1 comment -> 'id2 comment;
  map_size_spec         : ('a, 'id1, 'id2) t -> 'a -> 'id1 size_spec -> 'id2 size_spec;
  map_type_name         : ('a, 'id1, 'id2) t -> 'a -> 'id1 type_name -> 'id2 type_name;
  map_enumerator        : ('a, 'id1, 'id2) t -> 'a -> 'id1 enumerator -> 'id2 enumerator;
  map_error_list        : ('a, 'id1, 'id2) t -> 'a -> 'id1 error_list -> 'id2 error_list;
  map_parameter         : ('a, 'id1, 'id2) t -> 'a -> 'id1 parameter -> 'id2 parameter;
  map_expr              : ('a, 'id1, 'id2) t -> 'a -> 'id1 expr -> 'id2 expr;
  map_decl              : ('a, 'id1, 'id2) t -> 'a -> 'id1 decl -> 'id2 decl;
}


let visit_list f v state l =
  List.map (f v state) l


let visit_uname v state = function
  | name -> name

let visit_lname v state = function
  | name -> name

let visit_macro v state = function
  | Macro macro -> Macro macro


let visit_var v state = function
  | Var_UName uname ->
      let uname = v.map_uname v state uname in
      Var_UName uname
  | Var_LName lname ->
      let lname = v.map_lname v state lname in
      Var_LName lname
  | Var_Event ->
      Var_Event


let visit_comment_fragment v state = function
  | Cmtf_Doc doc ->
      Cmtf_Doc doc
  | Cmtf_UName uname ->
      let uname = v.map_uname v state uname in
      Cmtf_UName uname
  | Cmtf_LName lname ->
      let lname = v.map_lname v state lname in
      Cmtf_LName lname
  | Cmtf_Var var ->
      let var = visit_list v.map_var v state var in
      Cmtf_Var var
  | Cmtf_Break ->
      Cmtf_Break


let visit_comment v state = function
  | Cmt_None ->
      Cmt_None
  | Cmt_Doc frags ->
      let frags = visit_list v.map_comment_fragment v state frags in
      Cmt_Doc frags


let visit_size_spec v state = function
  | Ss_UName uname ->
      let uname = v.map_uname v state uname in
      Ss_UName uname
  | Ss_LName lname ->
      let lname = v.map_lname v state lname in
      Ss_LName lname
  | Ss_Bounded (size_spec, uname) ->
      let size_spec = v.map_lname v state size_spec in
      let uname = v.map_uname v state uname in
      Ss_Bounded (size_spec, uname)


let visit_type_name v state = function
  | Ty_UName uname ->
      let uname = v.map_uname v state uname in
      Ty_UName uname
  | Ty_LName lname ->
      let lname = v.map_lname v state lname in
      Ty_LName lname
  | Ty_Array (lname, size_spec) ->
      let lname = v.map_lname v state lname in
      let size_spec = v.map_size_spec v state size_spec in
      Ty_Array (lname, size_spec)
  | Ty_Auto ->
      Ty_Auto
  | Ty_Const type_name ->
      let type_name = v.map_type_name v state type_name in
      Ty_Const type_name
  | Ty_Pointer type_name ->
      let type_name = v.map_type_name v state type_name in
      Ty_Pointer type_name


let visit_enumerator v state = function
  | Enum_Name (comment, uname) ->
      let comment = v.map_comment v state comment in
      let uname = v.map_uname v state uname in
      Enum_Name (comment, uname)
  | Enum_Namespace (uname, enumerators) ->
      let uname = v.map_uname v state uname in
      let enumerators = visit_list v.map_enumerator v state enumerators in
      Enum_Namespace (uname, enumerators)


let visit_error_list v state = function
  | Err_None ->
      Err_None
  | Err_From lname ->
      let lname = v.map_lname v state lname in
      Err_From lname
  | Err_List enumerators ->
      let enumerators = visit_list v.map_enumerator v state enumerators in
      Err_List enumerators


let visit_parameter v state = function
  | Param (type_name, lname) ->
      let type_name = v.map_type_name v state type_name in
      let lname = v.map_lname v state lname in
      Param (type_name, lname)


let visit_expr v state = function
  | E_Number num ->
      E_Number num
  | E_UName uname ->
      let uname = v.map_uname v state uname in
      E_UName uname
  | E_Sizeof lname ->
      let lname = v.map_lname v state lname in
      E_Sizeof lname
  | E_Plus (lhs, rhs) ->
      let lhs = v.map_expr v state lhs in
      let rhs = v.map_expr v state rhs in
      E_Plus (lhs, rhs)


let visit_decl v state = function
  | Decl_Comment (comment, decl) ->
      let comment = v.map_comment v state comment in
      let decl = v.map_decl v state decl in
      Decl_Comment (comment, decl)
  | Decl_Inline decl ->
      let decl = v.map_decl v state decl in
      Decl_Inline decl
  | Decl_Static decl ->
      let decl = v.map_decl v state decl in
      Decl_Static decl
  | Decl_Macro macro ->
      let macro = v.map_macro v state macro in
      Decl_Macro macro
  | Decl_Namespace (lname, decls) ->
      let lname = v.map_lname v state lname in
      let decls = visit_list v.map_decl v state decls in
      Decl_Namespace (lname, decls)
  | Decl_Class (lname, decls) ->
      let lname = v.map_lname v state lname in
      let decls = visit_list v.map_decl v state decls in
      Decl_Class (lname, decls)
  | Decl_Function (type_name, lname, parameters, error_list) ->
      let type_name = v.map_type_name v state type_name in
      let lname = v.map_lname v state lname in
      let parameters = visit_list v.map_parameter v state parameters in
      let error_list = v.map_error_list v state error_list in
      Decl_Function (type_name, lname, parameters, error_list)
  | Decl_Const (uname, expr) ->
      let uname = v.map_uname v state uname in
      let expr = v.map_expr v state expr in
      Decl_Const (uname, expr)
  | Decl_Enum (is_class, uname, enumerators) ->
      let uname = v.map_uname v state uname in
      let enumerators = visit_list v.map_enumerator v state enumerators in
      Decl_Enum (is_class, uname, enumerators)
  | Decl_Error (lname, enumerators) ->
      let lname = v.map_lname v state lname in
      let enumerators = visit_list v.map_enumerator v state enumerators in
      Decl_Error (lname, enumerators)
  | Decl_Struct (lname, decls) ->
      let lname = v.map_lname v state lname in
      let decls = visit_list v.map_decl v state decls in
      Decl_Struct (lname, decls)
  | Decl_Member (type_name, lname) ->
      let type_name = v.map_type_name v state type_name in
      let lname = v.map_lname v state lname in
      Decl_Member (type_name, lname)
  | Decl_GetSet (type_name, lname, decls) ->
      let type_name = v.map_type_name v state type_name in
      let lname = v.map_lname v state lname in
      let decls = visit_list v.map_decl v state decls in
      Decl_GetSet (type_name, lname, decls)
  | Decl_Typedef (type_name, lname, parameters) ->
      let type_name = v.map_type_name v state type_name in
      let lname = v.map_lname v state lname in
      let parameters = visit_list v.map_parameter v state parameters in
      Decl_Typedef (type_name, lname, parameters)
  | Decl_Event (lname, decl) ->
      let lname = v.map_lname v state lname in
      let decl = visit_list v.map_decl v state decl in
      Decl_Event (lname, decl)
  | Decl_Section frags ->
      let frags = visit_list v.map_comment_fragment v state frags in
      Decl_Section frags


let visit_decls v state = visit_list v.map_decl v state


let default = {
  map_uname = visit_uname;
  map_lname = visit_lname;
  map_macro = visit_macro;
  map_var = visit_var;
  map_comment_fragment = visit_comment_fragment;
  map_comment = visit_comment;
  map_size_spec = visit_size_spec;
  map_type_name = visit_type_name;
  map_enumerator = visit_enumerator;
  map_error_list = visit_error_list;
  map_parameter = visit_parameter;
  map_expr = visit_expr;
  map_decl = visit_decl;
}
