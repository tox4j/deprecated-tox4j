open ApiAst


type ('a, 'id1, 'id2) t = {
  fold_uname            : ('a, 'id1, 'id2) t -> 'a -> 'id1 uname -> 'a * 'id2 uname;
  fold_lname            : ('a, 'id1, 'id2) t -> 'a -> 'id1 lname -> 'a * 'id2 lname;
  fold_macro            : ('a, 'id1, 'id2) t -> 'a -> macro -> 'a * macro;
  fold_var              : ('a, 'id1, 'id2) t -> 'a -> 'id1 var -> 'a * 'id2 var;
  fold_comment_fragment : ('a, 'id1, 'id2) t -> 'a -> 'id1 comment_fragment -> 'a * 'id2 comment_fragment;
  fold_comment          : ('a, 'id1, 'id2) t -> 'a -> 'id1 comment -> 'a * 'id2 comment;
  fold_size_spec        : ('a, 'id1, 'id2) t -> 'a -> 'id1 size_spec -> 'a * 'id2 size_spec;
  fold_type_name        : ('a, 'id1, 'id2) t -> 'a -> 'id1 type_name -> 'a * 'id2 type_name;
  fold_enumerator       : ('a, 'id1, 'id2) t -> 'a -> 'id1 enumerator -> 'a * 'id2 enumerator;
  fold_error_list       : ('a, 'id1, 'id2) t -> 'a -> 'id1 error_list -> 'a * 'id2 error_list;
  fold_parameter        : ('a, 'id1, 'id2) t -> 'a -> 'id1 parameter -> 'a * 'id2 parameter;
  fold_expr             : ('a, 'id1, 'id2) t -> 'a -> 'id1 expr -> 'a * 'id2 expr;
  fold_decl             : ('a, 'id1, 'id2) t -> 'a -> 'id1 decl -> 'a * 'id2 decl;
}


let visit_list f v state l =
  let state, l =
    List.fold_left
      (fun (state, l) elt ->
         let state, elt = f v state elt in
         state, elt :: l
      ) (state, []) l
  in
  state, List.rev l


let visit_uname v state = function
  | name -> state, name

let visit_lname v state = function
  | name -> state, name

let visit_macro v state = function
  | Macro macro -> state, Macro macro


let visit_var v state = function
  | Var_UName uname ->
      let state, uname = v.fold_uname v state uname in
      state, Var_UName uname
  | Var_LName lname ->
      let state, lname = v.fold_lname v state lname in
      state, Var_LName lname
  | Var_Event ->
      state, Var_Event


let visit_comment_fragment v state = function
  | Cmtf_Doc doc ->
      state, Cmtf_Doc doc
  | Cmtf_UName uname ->
      let state, uname = v.fold_uname v state uname in
      state, Cmtf_UName uname
  | Cmtf_LName lname ->
      let state, lname = v.fold_lname v state lname in
      state, Cmtf_LName lname
  | Cmtf_Var var ->
      let state, var = visit_list v.fold_var v state var in
      state, Cmtf_Var var
  | Cmtf_Break ->
      state, Cmtf_Break


let visit_comment v state = function
  | Cmt_None ->
      state, Cmt_None
  | Cmt_Doc frags ->
      let state, frags = visit_list v.fold_comment_fragment v state frags in
      state, Cmt_Doc frags


let visit_size_spec v state = function
  | Ss_UName uname ->
      let state, uname = v.fold_uname v state uname in
      state, Ss_UName uname
  | Ss_LName lname ->
      let state, lname = v.fold_lname v state lname in
      state, Ss_LName lname
  | Ss_Bounded (size_spec, uname) ->
      let state, size_spec = v.fold_lname v state size_spec in
      let state, uname = v.fold_uname v state uname in
      state, Ss_Bounded (size_spec, uname)


let visit_type_name v state = function
  | Ty_UName uname ->
      let state, uname = v.fold_uname v state uname in
      state, Ty_UName uname
  | Ty_LName lname ->
      let state, lname = v.fold_lname v state lname in
      state, Ty_LName lname
  | Ty_Array (lname, size_spec) ->
      let state, lname = v.fold_lname v state lname in
      let state, size_spec = v.fold_size_spec v state size_spec in
      state, Ty_Array (lname, size_spec)
  | Ty_Auto ->
      state, Ty_Auto
  | Ty_Const type_name ->
      let state, type_name = v.fold_type_name v state type_name in
      state, Ty_Const type_name
  | Ty_Pointer type_name ->
      let state, type_name = v.fold_type_name v state type_name in
      state, Ty_Pointer type_name


let visit_enumerator v state = function
  | Enum_Name (comment, uname) ->
      let state, comment = v.fold_comment v state comment in
      let state, uname = v.fold_uname v state uname in
      state, Enum_Name (comment, uname)
  | Enum_Namespace (uname, enumerators) ->
      let state, uname = v.fold_uname v state uname in
      let state, enumerators = visit_list v.fold_enumerator v state enumerators in
      state, Enum_Namespace (uname, enumerators)


let visit_error_list v state = function
  | Err_None ->
      state, Err_None
  | Err_From lname ->
      let state, lname = v.fold_lname v state lname in
      state, Err_From lname
  | Err_List enumerators ->
      let state, enumerators = visit_list v.fold_enumerator v state enumerators in
      state, Err_List enumerators


let visit_parameter v state = function
  | Param (type_name, lname) ->
      let state, type_name = v.fold_type_name v state type_name in
      let state, lname = v.fold_lname v state lname in
      state, Param (type_name, lname)


let visit_expr v state = function
  | E_Number num ->
      state, E_Number num
  | E_UName uname ->
      let state, uname = v.fold_uname v state uname in
      state, E_UName uname
  | E_Sizeof lname ->
      let state, lname = v.fold_lname v state lname in
      state, E_Sizeof lname
  | E_Plus (lhs, rhs) ->
      let state, lhs = v.fold_expr v state lhs in
      let state, rhs = v.fold_expr v state rhs in
      state, E_Plus (lhs, rhs)


let visit_decl v state = function
  | Decl_Comment (comment, decl) ->
      let state, comment = v.fold_comment v state comment in
      let state, decl = v.fold_decl v state decl in
      state, Decl_Comment (comment, decl)
  | Decl_Inline decl ->
      let state, decl = v.fold_decl v state decl in
      state, Decl_Inline decl
  | Decl_Static decl ->
      let state, decl = v.fold_decl v state decl in
      state, Decl_Static decl
  | Decl_Macro macro ->
      let state, macro = v.fold_macro v state macro in
      state, Decl_Macro macro
  | Decl_Namespace (lname, decls) ->
      let state, lname = v.fold_lname v state lname in
      let state, decls = visit_list v.fold_decl v state decls in
      state, Decl_Namespace (lname, decls)
  | Decl_Class (lname, decls) ->
      let state, lname = v.fold_lname v state lname in
      let state, decls = visit_list v.fold_decl v state decls in
      state, Decl_Class (lname, decls)
  | Decl_Function (type_name, lname, parameters, error_list) ->
      let state, type_name = v.fold_type_name v state type_name in
      let state, lname = v.fold_lname v state lname in
      let state, parameters = visit_list v.fold_parameter v state parameters in
      let state, error_list = v.fold_error_list v state error_list in
      state, Decl_Function (type_name, lname, parameters, error_list)
  | Decl_Const (uname, expr) ->
      let state, uname = v.fold_uname v state uname in
      let state, expr = v.fold_expr v state expr in
      state, Decl_Const (uname, expr)
  | Decl_Enum (is_class, uname, enumerators) ->
      let state, uname = v.fold_uname v state uname in
      let state, enumerators = visit_list v.fold_enumerator v state enumerators in
      state, Decl_Enum (is_class, uname, enumerators)
  | Decl_Error (lname, enumerators) ->
      let state, lname = v.fold_lname v state lname in
      let state, enumerators = visit_list v.fold_enumerator v state enumerators in
      state, Decl_Error (lname, enumerators)
  | Decl_Struct (lname, decls) ->
      let state, lname = v.fold_lname v state lname in
      let state, decls = visit_list v.fold_decl v state decls in
      state, Decl_Struct (lname, decls)
  | Decl_Member (type_name, lname) ->
      let state, type_name = v.fold_type_name v state type_name in
      let state, lname = v.fold_lname v state lname in
      state, Decl_Member (type_name, lname)
  | Decl_GetSet (type_name, lname, decls) ->
      let state, type_name = v.fold_type_name v state type_name in
      let state, lname = v.fold_lname v state lname in
      let state, decls = visit_list v.fold_decl v state decls in
      state, Decl_GetSet (type_name, lname, decls)
  | Decl_Typedef (type_name, lname, parameters) ->
      let state, type_name = v.fold_type_name v state type_name in
      let state, lname = v.fold_lname v state lname in
      let state, parameters = visit_list v.fold_parameter v state parameters in
      state, Decl_Typedef (type_name, lname, parameters)
  | Decl_Event (lname, decl) ->
      let state, lname = v.fold_lname v state lname in
      let state, decl = visit_list v.fold_decl v state decl in
      state, Decl_Event (lname, decl)
  | Decl_Section frags ->
      let state, frags = visit_list v.fold_comment_fragment v state frags in
      state, Decl_Section frags


let visit_decls v state = visit_list v.fold_decl v state


let make ~fold_uname ~fold_lname = {
  fold_uname;
  fold_lname;
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
