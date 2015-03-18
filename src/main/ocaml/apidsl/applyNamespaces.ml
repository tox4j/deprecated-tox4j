open ApiAst
open ApiFold


let prepend_ns ns name =
  List.fold_left
    (fun name ns ->
       ns ^ "_" ^ name
    ) name ns


let resolve_ns symtab ns =
  List.map (SymbolTable.name symtab) ns


let fold_decl v (symtab, ignore_first, ns) = function
  | Decl_Enum (_, uname, _)
  | Decl_Const (uname, _) ->
      let symtab =
        resolve_ns symtab ns
        |> List.map String.uppercase
        |> prepend_ns
        |> SymbolTable.rename symtab uname
      in

      (symtab, ignore_first, ns)

  | Decl_Struct decls ->
      (* Reset namespace for struct members. *)
      let symtab, _, _ = visit_list v.fold_decl v (symtab, ignore_first, []) decls in
      (symtab, ignore_first, ns)

  | Decl_Member (_, lname)
  | Decl_Function (_, lname, _, _) ->
      let symtab =
        resolve_ns symtab ns
        |> prepend_ns
        |> SymbolTable.rename symtab lname
      in

      (symtab, ignore_first, ns)

  | Decl_Namespace (name, decls) ->
      let symtab, _, _ =
        if ignore_first = 0 then
          visit_list v.fold_decl v (symtab, ignore_first, name :: ns) decls
        else
          visit_list v.fold_decl v (symtab, ignore_first - 1, ns) decls
      in
      (symtab, ignore_first, ns)

  | decl ->
      visit_decl v (symtab, ignore_first, ns) decl


let v = { default with fold_decl }


let transform ignore_first (symtab, decls) =
  let symtab, _, _ =
    visit_decls v (symtab, ignore_first, []) decls
  in
  symtab, decls
