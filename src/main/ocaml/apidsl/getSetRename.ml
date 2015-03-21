open ApiAst
open ApiFold


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

  | Decl_Error (uname, _) ->
      let name = String.uppercase @@ SymbolTable.name symtab name in

      SymbolTable.rename symtab uname
        (fun error_name ->
           error_name ^ "_" ^ name)

  | decl ->
      failwith @@ show_decl (SymbolTable.pp_symbol symtab) decl


let fold_decl v symtab = function
  | Decl_GetSet (type_name, lname, decls) ->
      List.fold_left (rename_symbols lname) symtab decls

  | decl ->
      visit_decl v symtab decl


let v = { default with fold_decl }


let transform (symtab, decls) =
  visit_decls v symtab decls, decls
