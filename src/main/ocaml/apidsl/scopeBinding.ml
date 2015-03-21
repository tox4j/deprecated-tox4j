open ApiAst
open ApiMap


let flip f a b = f b a


let scoped scopes name f =
  let scopes = name :: scopes in
  f scopes


let map_uname symtab v scopes uname =
  SymbolTable.lookup symtab scopes uname


let map_lname symtab v scopes lname =
  if String.length lname > 3 &&
     String.sub lname (String.length lname - 2) 2 = "_t" then
    (* First, it might be a global _t type. *)
    match SymbolTable.lookup symtab [] lname with
    | -1 ->
        (* If not, it must be a user-defined type with "this" struct. *)
        let ns = String.sub lname 0 (String.length lname - 2) in
        let scopes = ns :: List.tl scopes in
        SymbolTable.lookup symtab scopes "this"
    | resolved ->
        resolved
  else
    SymbolTable.lookup symtab scopes lname


let map_enumerator symtab v scopes = function
  | Enum_Name _ as enumerator ->
      visit_enumerator v scopes enumerator

  | Enum_Namespace (uname, enumerators) ->
      let uname' = v.map_uname v scopes uname in
      let enumerators = scoped scopes uname (flip (visit_list v.map_enumerator v) enumerators) in
      Enum_Namespace (uname', enumerators)


let map_error_list symtab v scopes = function
  | Err_From lname ->
      let lname = "error " ^ lname in
      let lname' = v.map_lname v scopes lname in
      Err_From lname'

  | error_list ->
      visit_error_list v scopes error_list


let map_decl symtab v scopes = function
  | Decl_Namespace (lname, decls) ->
      let lname' = v.map_lname v scopes lname in
      let decls = scoped scopes lname (flip (visit_list v.map_decl v) decls) in
      Decl_Namespace (lname', decls)
  | Decl_Class (lname, decls) ->
      let lname' = v.map_lname v scopes lname in
      let decls = scoped scopes lname (flip (visit_list v.map_decl v) decls) in
      Decl_Class (lname', decls)
  | Decl_Function (type_name, lname, parameters, error_list) ->
      let type_name = v.map_type_name v scopes type_name in
      let lname' = v.map_lname v scopes lname in
      let parameters = scoped scopes lname (flip (visit_list v.map_parameter v) parameters) in
      let error_list = scoped scopes lname (flip (v.map_error_list v) error_list) in
      Decl_Function (type_name, lname', parameters, error_list)
  | Decl_Enum (is_class, uname, enumerators) ->
      let uname' = v.map_uname v scopes uname in
      let enumerators = scoped scopes uname (flip (visit_list v.map_enumerator v) enumerators) in
      Decl_Enum (is_class, uname', enumerators)
  | Decl_Error (lname, enumerators) ->
      let lname = "error " ^ lname in
      let lname' = v.map_lname v scopes lname in
      let enumerators = scoped scopes lname (flip (visit_list v.map_enumerator v) enumerators) in
      Decl_Error (lname', enumerators)
  | Decl_Struct (lname, decls) ->
      let lname' = v.map_lname v scopes lname in
      let decls = scoped scopes lname (flip (visit_list v.map_decl v) decls) in
      Decl_Struct (lname', decls)
  | Decl_GetSet (type_name, lname, decls) ->
      let type_name = scoped scopes lname (flip (v.map_type_name v) type_name) in
      let lname' = v.map_lname v scopes lname in
      let decls = scoped scopes lname (flip (visit_list v.map_decl v) decls) in
      Decl_GetSet (type_name, lname', decls)
  | Decl_Event (lname, decls) ->
      let lname = "event " ^ lname in
      let lname' = v.map_lname v scopes lname in
      let decls = scoped scopes lname (flip (visit_list v.map_decl v) decls) in
      Decl_Event (lname', decls)
  | Decl_Typedef (type_name, lname, parameters) ->
      let type_name = scoped scopes lname (flip (v.map_type_name v) type_name) in
      let lname' = v.map_lname v scopes lname in
      let parameters = scoped scopes lname (flip (visit_list v.map_parameter v) parameters) in
      Decl_Typedef (type_name, lname', parameters)

  | Decl_Const _
  | Decl_Member _
  | Decl_Section _
  | Decl_Comment _
  | Decl_Inline _
  | Decl_Static _
  | Decl_Macro _ as decl ->
      ApiMap.visit_decl v scopes decl


let v symtab = {
  map_uname = map_uname symtab;
  map_lname = map_lname symtab;
  map_enumerator = map_enumerator symtab;
  map_decl = map_decl symtab;
  map_error_list = map_error_list symtab;

  map_var = visit_var;
  map_macro = visit_macro;
  map_comment_fragment = visit_comment_fragment;
  map_comment = visit_comment;
  map_size_spec = visit_size_spec;
  map_type_name = visit_type_name;
  map_parameter = visit_parameter;
  map_expr = visit_expr;
}


let transform (symtab, decls) =
  symtab, visit_decls (v symtab) [] decls
  

module Inverse = struct

  let map_uname v symtab uname =
    SymbolTable.name symtab uname


  let map_lname v symtab lname =
    SymbolTable.name symtab lname


  let v = {
    map_uname;
    map_lname;

    map_var = visit_var;
    map_enumerator = visit_enumerator;
    map_decl = visit_decl;
    map_macro = visit_macro;
    map_comment_fragment = visit_comment_fragment;
    map_comment = visit_comment;
    map_size_spec = visit_size_spec;
    map_type_name = visit_type_name;
    map_error_list = visit_error_list;
    map_parameter = visit_parameter;
    map_expr = visit_expr;
  }

  let transform (symtab, decls) =
    visit_decls v symtab decls

end
