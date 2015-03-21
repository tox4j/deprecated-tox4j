open ApiAst


type ('a, 'id) t = {
  fold_uname            : ('a, 'id) t -> 'a -> 'id uname -> 'a;
  fold_lname            : ('a, 'id) t -> 'a -> 'id lname -> 'a;
  fold_macro            : ('a, 'id) t -> 'a -> macro -> 'a;
  fold_var              : ('a, 'id) t -> 'a -> 'id var -> 'a;
  fold_comment_fragment : ('a, 'id) t -> 'a -> 'id comment_fragment -> 'a;
  fold_comment          : ('a, 'id) t -> 'a -> 'id comment -> 'a;
  fold_size_spec        : ('a, 'id) t -> 'a -> 'id size_spec -> 'a;
  fold_type_name        : ('a, 'id) t -> 'a -> 'id type_name -> 'a;
  fold_enumerator       : ('a, 'id) t -> 'a -> 'id enumerator -> 'a;
  fold_error_list       : ('a, 'id) t -> 'a -> 'id error_list -> 'a;
  fold_parameter        : ('a, 'id) t -> 'a -> 'id parameter -> 'a;
  fold_expr             : ('a, 'id) t -> 'a -> 'id expr -> 'a;
  fold_decl             : ('a, 'id) t -> 'a -> 'id decl -> 'a;
}


let visit_list f v state l =
  List.fold_left (f v) state l


let visit_uname v state = function
  | name -> state

let visit_lname v state = function
  | name -> state

let visit_macro v state = function
  | Macro macro -> state


let visit_var v state = function
  | Var_UName uname ->
      let state = v.fold_uname v state uname in
      state
  | Var_LName lname ->
      let state = v.fold_lname v state lname in
      state
  | Var_Event ->
      state


let visit_comment_fragment v state = function
  | Cmtf_Doc doc ->
      state
  | Cmtf_UName uname ->
      let state = v.fold_uname v state uname in
      state
  | Cmtf_LName lname ->
      let state = v.fold_lname v state lname in
      state
  | Cmtf_Var var ->
      let state = visit_list v.fold_var v state var in
      state
  | Cmtf_Break ->
      state


let visit_comment v state = function
  | Cmt_None ->
      state
  | Cmt_Doc frags ->
      let state = visit_list v.fold_comment_fragment v state frags in
      state


let visit_size_spec v state = function
  | Ss_UName uname ->
      let state = v.fold_uname v state uname in
      state
  | Ss_LName lname ->
      let state = v.fold_lname v state lname in
      state
  | Ss_Bounded (size_spec, uname) ->
      let state = v.fold_lname v state size_spec in
      let state = v.fold_uname v state uname in
      state


let visit_type_name v state = function
  | Ty_UName uname ->
      let state = v.fold_uname v state uname in
      state
  | Ty_LName lname ->
      let state = v.fold_lname v state lname in
      state
  | Ty_Array (lname, size_spec) ->
      let state = v.fold_lname v state lname in
      let state = v.fold_size_spec v state size_spec in
      state
  | Ty_Auto ->
      state
  | Ty_Const type_name ->
      let state = v.fold_type_name v state type_name in
      state
  | Ty_Pointer type_name ->
      let state = v.fold_type_name v state type_name in
      state


let visit_enumerator v state = function
  | Enum_Name (comment, uname) ->
      let state = v.fold_comment v state comment in
      let state = v.fold_uname v state uname in
      state
  | Enum_Namespace (uname, enumerators) ->
      let state = v.fold_uname v state uname in
      let state = visit_list v.fold_enumerator v state enumerators in
      state


let visit_error_list v state = function
  | Err_None ->
      state
  | Err_From lname ->
      let state = v.fold_lname v state lname in
      state
  | Err_List enumerators ->
      let state = visit_list v.fold_enumerator v state enumerators in
      state


let visit_parameter v state = function
  | Param (type_name, lname) ->
      let state = v.fold_type_name v state type_name in
      let state = v.fold_lname v state lname in
      state


let visit_expr v state = function
  | E_Number num ->
      state
  | E_UName uname ->
      let state = v.fold_uname v state uname in
      state
  | E_Sizeof lname ->
      let state = v.fold_lname v state lname in
      state
  | E_Plus (lhs, rhs) ->
      let state = v.fold_expr v state lhs in
      let state = v.fold_expr v state rhs in
      state


let visit_decl v state = function
  | Decl_Comment (comment, decl) ->
      let state = v.fold_comment v state comment in
      let state = v.fold_decl v state decl in
      state
  | Decl_Inline decl ->
      let state = v.fold_decl v state decl in
      state
  | Decl_Static decl ->
      let state = v.fold_decl v state decl in
      state
  | Decl_Macro macro ->
      let state = v.fold_macro v state macro in
      state
  | Decl_Namespace (lname, decls) ->
      let state = v.fold_lname v state lname in
      let state = visit_list v.fold_decl v state decls in
      state
  | Decl_Class (lname, decls) ->
      let state = v.fold_lname v state lname in
      let state = visit_list v.fold_decl v state decls in
      state
  | Decl_Function (type_name, lname, parameters, error_list) ->
      let state = v.fold_type_name v state type_name in
      let state = v.fold_lname v state lname in
      let state = visit_list v.fold_parameter v state parameters in
      let state = v.fold_error_list v state error_list in
      state
  | Decl_Const (uname, expr) ->
      let state = v.fold_uname v state uname in
      let state = v.fold_expr v state expr in
      state
  | Decl_Enum (is_class, uname, enumerators) ->
      let state = v.fold_uname v state uname in
      let state = visit_list v.fold_enumerator v state enumerators in
      state
  | Decl_Error (lname, enumerators) ->
      let state = v.fold_lname v state lname in
      let state = visit_list v.fold_enumerator v state enumerators in
      state
  | Decl_Struct (lname, decls) ->
      let state = v.fold_lname v state lname in
      let state = visit_list v.fold_decl v state decls in
      state
  | Decl_Member (type_name, lname) ->
      let state = v.fold_type_name v state type_name in
      let state = v.fold_lname v state lname in
      state
  | Decl_GetSet (type_name, lname, decls) ->
      let state = v.fold_type_name v state type_name in
      let state = v.fold_lname v state lname in
      let state = visit_list v.fold_decl v state decls in
      state
  | Decl_Typedef (type_name, lname, parameters) ->
      let state = v.fold_type_name v state type_name in
      let state = v.fold_lname v state lname in
      let state = visit_list v.fold_parameter v state parameters in
      state
  | Decl_Event (lname, decl) ->
      let state = v.fold_lname v state lname in
      let state = visit_list v.fold_decl v state decl in
      state
  | Decl_Section frags ->
      let state = visit_list v.fold_comment_fragment v state frags in
      state


let visit_decls v state = visit_list v.fold_decl v state


let default = {
  fold_uname = visit_uname;
  fold_lname = visit_lname;
  fold_macro = visit_macro;
  fold_var = visit_var;
  fold_comment_fragment = visit_comment_fragment;
  fold_comment = visit_comment;
  fold_size_spec = visit_size_spec;
  fold_type_name = visit_type_name;
  fold_enumerator = visit_enumerator;
  fold_error_list = visit_error_list;
  fold_parameter = visit_parameter;
  fold_expr = visit_expr;
  fold_decl = visit_decl;
}
