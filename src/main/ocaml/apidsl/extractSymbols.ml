open ApiAst


let fold_scopedl f v lname decls scope =
  SymbolTable.scopedl scope lname
    (ApiFold.visit_list f v) decls


let fold_scopedu f v uname decls scope =
  SymbolTable.scopedu scope uname
    (ApiFold.visit_list f v) decls


let extract decls =
  let open ApiFold in


  let fold_function_name v scope = function
    | Fn_Custom (type_name, lname) ->
        let scope = SymbolTable.addl lname scope in
        scope
  in


  let fold_decl v scope = function
    | Decl_GetSet (_, lname, decls)
    | Decl_Class (lname, decls) ->
        scope
        |> SymbolTable.addl lname
        |> fold_scopedl v.fold_decl v lname decls

    | Decl_Namespace (lname, decls) ->
        scope
        |> SymbolTable.addl ~extend:true lname
        |> fold_scopedl v.fold_decl v lname decls

    | Decl_Enum (_, uname, enumerators) ->
        scope
        |> SymbolTable.addu uname
        |> fold_scopedu v.fold_enumerator v uname enumerators

    | decl ->
        ApiFold.visit_decl v scope decl
  in

  let v = {
    default with
    fold_function_name;
    fold_decl;
  } in
  SymbolTable.make @@ visit_decls v SymbolTable.empty decls
