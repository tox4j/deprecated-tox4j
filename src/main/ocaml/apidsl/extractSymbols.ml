open ApiAst


let extract decls =
  let open ApiFold in


  let fold_function_name v scope = function
    | Fn_Custom (type_name, lname) as fn ->
        let scope = SymbolTable.addl scope lname in
        scope, fn
  in


  let scoped_fold_decls v scope lname decls =
    fst @@ SymbolTable.scopedl scope lname
      (visit_list v.fold_decl v) decls
  in

  let fold_decl v scope = function
    | Decl_GetSet (_, lname, decls)
    | Decl_Class (lname, decls) as decl ->
        let scope = SymbolTable.addl scope lname in
        let scope = scoped_fold_decls v scope lname decls in
        scope, decl

    | Decl_Namespace (lname, decls) as decl ->
        let scope = SymbolTable.addl ~extend:true scope lname in
        let scope = scoped_fold_decls v scope lname decls in
        scope, decl

    | decl ->
        ApiFold.visit_decl v scope decl
  in

  let v = {
    default with
    fold_function_name;
    fold_decl;
  } in
  SymbolTable.make @@ fst @@ visit_decls v SymbolTable.empty decls
