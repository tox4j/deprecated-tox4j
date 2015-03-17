open ApiAst


let extract decls =
  let open ApiFold in


  let fold_function_name v symtab = function
    | Fn_Custom (type_name, lname) as fn ->
        let symtab = SymbolTable.addl symtab lname in
        symtab, fn
  in


  let scoped_fold_decls v symtab lname decls =
    fst @@ SymbolTable.scopedl symtab lname
      (visit_list v.fold_decl v) decls
  in

  let fold_decl v symtab = function
    | Decl_GetSet (_, lname, decls) as decl ->
        let symtab = SymbolTable.addl symtab lname in
        let symtab = scoped_fold_decls v symtab lname decls in
        symtab, decl

    | Decl_Class (lname, decls) as decl ->
        let symtab = scoped_fold_decls v symtab lname decls in
        symtab, decl

    | Decl_Namespace (lname, decls) as decl ->
        let symtab = scoped_fold_decls v symtab lname decls in
        symtab, decl

    | decl ->
        ApiFold.visit_decl v symtab decl
  in

  let v = {
    default with
    fold_function_name;
    fold_decl;
  } in
  SymbolTable.make @@ fst @@ visit_decls v SymbolTable.empty decls
