open ApiAst
open ApiFold


let fold_decl v (symtab, ns) = function

  | Decl_Class (name, decls) ->
      let (symtab, _) =
        visit_list v.fold_decl v (symtab, name :: ns) decls
      in
      (symtab, ns)

  | Decl_Namespace (name, decls) ->
      let (symtab, _) =
        visit_list v.fold_decl v (symtab, ns) decls
      in
      (symtab, ns)

  | Decl_Struct (this, decls) ->
      let ns_names = List.map (SymbolTable.name symtab) ns in

      let symtab =
        let class_name =
          ns_names
          |> List.rev
          |> List.map String.capitalize
          |> String.concat "_"
        in
        SymbolTable.rename symtab this
          (fun x -> class_name)
      in

      (symtab, ns)

  | decl ->
      visit_decl v (symtab, ns) decl


let v = { default with fold_decl }


let transform (symtab, decls) =
  let (symtab, _) = visit_decls v (symtab, []) decls in

  symtab, decls
