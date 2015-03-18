open ApiAst
open ApiFoldMap


let rec rename_symbols name symtab = function
  | Decl_Comment (comment, decl) ->
      rename_symbols name symtab decl

  | Decl_Function (_, fname, _, _) as decl ->
      let name = SymbolTable.name symtab name in

      begin match SymbolTable.name symtab fname with
        | "size" ->
            SymbolTable.rename symtab fname
              (fun _ ->
                 if name = "this" then
                   "get_size"
                 else
                   "get_" ^ name ^ "_size")

        | "get" ->
            SymbolTable.rename symtab fname
              (fun _ ->
                 if name = "this" then
                   "get"
                 else
                   "get_" ^ name)

        | "set" ->
            SymbolTable.rename symtab fname
              (fun _ ->
                 if name = "this" then
                   "set"
                 else
                   "set_" ^ name)

        | _ ->
            failwith (
              "Unknown function: " ^
              show_decl (SymbolTable.pp_symbol symtab) decl
            )
      end

  | Decl_Error _ ->
      symtab

  | decl ->
      failwith @@ show_decl (SymbolTable.pp_symbol symtab) decl


let fold_decl v state = function
  | Decl_GetSet (type_name, lname, decls) ->
      let symtab = ReplaceDecl.get state in

      let symtab = List.fold_left (rename_symbols lname) symtab decls in

      let state = ReplaceDecl.set state symtab in
      let state = ReplaceDecl.replace state decls in

      state, Decl_GetSet (type_name, lname, decls)

  | decl ->
      ReplaceDecl.fold_decl v state decl


let v = { default with fold_decl }


let transform (symtab, decls) =
  let state, decls =
    ReplaceDecl.fold_decls v (ReplaceDecl.initial, symtab) decls
  in
  ReplaceDecl.get state, decls
