open ApiAst
open ApiFold


let prepend_ns ns name =
  List.fold_left
    (fun name ns ->
       ns ^ "_" ^ name
    ) name ns


let resolve_ns symtab ns =
  List.map (SymbolTable.name symtab) ns


let fold_decl v (symtab, ns) = function
  | Decl_Const (uname, _) ->
      let symtab =
        resolve_ns symtab ns
        |> List.map String.uppercase
        |> prepend_ns
        |> SymbolTable.rename symtab uname
      in

      (symtab, ns)

  | Decl_Struct decls ->
      (* Reset namespace for struct members. *)
      let symtab, _ = visit_list v.fold_decl v (symtab, []) decls in
      (symtab, ns)

  | Decl_Member (_, lname)
  | Decl_Function (_, lname, _, _) ->
      let symtab =
        resolve_ns symtab ns
        |> prepend_ns
        |> SymbolTable.rename symtab lname
      in

      (symtab, ns)

  | Decl_Namespace (name, decls) ->
        let symtab, _ = visit_list v.fold_decl v (symtab, name :: ns) decls in
        (symtab, ns)

  | decl ->
      visit_decl v (symtab, ns) decl


let v = { default with fold_decl }


let transform (symtab, decls) =
  let symtab, _ =
    visit_decls v (symtab, []) decls
  in
  symtab, decls
