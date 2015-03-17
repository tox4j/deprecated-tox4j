open ApiAst


let flip f a b = f b a


let this = Name.lname "this"


let scopedl scopes lname f =
  let name = LName.to_string lname in
  let scopes = name :: scopes in
  f scopes


let scopedu scopes uname f =
  let name = UName.to_string uname in
  let scopes = name :: scopes in
  f scopes
  


let transform symtab decls =
  let open ApiMap in


  let map_uname v scopes uname =
    let name = UName.to_string uname in
    let id = SymbolTable.lookup symtab scopes name in
    Name.uid id
  in


  let map_lname v scopes lname =
    let name = LName.to_string lname in
    let id = SymbolTable.lookup symtab scopes name in
    Name.lid id
  in


  let map_enumerator v scopes = function
    | Enum_Name _ as enumerator ->
        visit_enumerator v scopes enumerator

    | Enum_Namespace (uname, enumerators) ->
        let uname' = v.map_uname v scopes uname in
        let enumerators = scopedu scopes uname (flip (visit_list v.map_enumerator v) enumerators) in
        Enum_Namespace (uname', enumerators)
  in


  let map_decl (v : (string list, string, int) ApiMap.t) scopes = function
    | Decl_Namespace (lname, decls) ->
        let lname' = v.map_lname v scopes lname in
        let decls = scopedl scopes lname (flip (visit_list v.map_decl v) decls) in
        Decl_Namespace (lname', decls)
    | Decl_Class (lname, decls) ->
        let lname' = v.map_lname v scopes lname in
        let decls = scopedl scopes lname (flip (visit_list v.map_decl v) decls) in
        Decl_Class (lname', decls)
    | Decl_Function (type_name, lname, parameters, error_list) ->
        let type_name = v.map_type_name v scopes type_name in
        let lname' = v.map_lname v scopes lname in
        let parameters = scopedl scopes lname (flip (visit_list v.map_parameter v) parameters) in
        let error_list = scopedl scopes lname (flip (v.map_error_list v) error_list) in
        Decl_Function (type_name, lname', parameters, error_list)
    | Decl_Enum (is_class, uname, enumerators) ->
        let uname' = v.map_uname v scopes uname in
        let enumerators = scopedu scopes uname (flip (visit_list v.map_enumerator v) enumerators) in
        Decl_Enum (is_class, uname', enumerators)
    | Decl_Error (lname, enumerators) ->
        let lname' = v.map_lname v scopes lname in
        let enumerators = scopedl scopes lname (flip (visit_list v.map_enumerator v) enumerators) in
        Decl_Error (lname', enumerators)
    | Decl_Struct decls ->
        let decls = scopedl scopes this (flip (visit_list v.map_decl v) decls) in
        Decl_Struct decls
    | Decl_GetSet (type_name, lname, decls) ->
        let type_name = scopedl scopes lname (flip (v.map_type_name v) type_name) in
        let lname' = v.map_lname v scopes lname in
        let decls = scopedl scopes lname (flip (visit_list v.map_decl v) decls) in
        Decl_GetSet (type_name, lname', decls)
    | Decl_Event (lname, decl) ->
        let lname = LName.prepend "event " lname in
        let lname' = v.map_lname v scopes lname in
        let decl = scopedl scopes lname (flip (v.map_decl v) decl) in
        Decl_Event (lname', decl)

    | Decl_Const _
    | Decl_Member _
    | Decl_Comment _
    | Decl_Static _
    | Decl_Macro _
    | Decl_Typedef _ as decl ->
        ApiMap.visit_decl v scopes decl
  in

  let v = {
    map_uname;
    map_lname;
    map_enumerator;
    map_decl;

    map_macro = visit_macro;
    map_comment_fragment = visit_comment_fragment;
    map_comment = visit_comment;
    map_size_spec = visit_size_spec;
    map_type_name = visit_type_name;
    map_error_list = visit_error_list;
    map_parameter = visit_parameter;
    map_expr = visit_expr;
  } in
  visit_decls v [] decls
  


let inverse symtab decls =
  let open ApiMap in


  let map_uname v () uname =
    Name.repr uname |> SymbolTable.uname symtab
  in


  let map_lname v () lname =
    Name.repr lname |> SymbolTable.lname symtab
  in


  let v = {
    map_uname;
    map_lname;

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
  } in
  visit_decls v () decls
