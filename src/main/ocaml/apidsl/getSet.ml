open ApiAst
open ApiFoldMap


let rec add_types symtab name ty = function
  | Decl_Comment (comment, decl) ->
      let decl = add_types symtab name ty decl in
      Decl_Comment (comment, decl)

  | Decl_Function (Ty_Auto, fname, parameters, error_list) as decl ->
      let size_t = TypeName.size_t_ symtab in
      let void = TypeName.void_ symtab in

      begin match SymbolTable.name symtab fname with
        | "size" ->
            Decl_Function (size_t, fname, parameters, error_list)

        | "get" ->
            if TypeName.is_array ty then
              let parameters =
                if TypeName.is_var_array ty then
                  Param (size_t, TypeName.length_param ty) :: parameters
                else
                  parameters
              in
              let parameters = Param (ty, name) :: parameters in
              Decl_Function (void, fname, parameters, error_list)

            else
              Decl_Function (ty, fname, parameters, error_list)

        | "set" ->
            let parameters =
              if TypeName.is_var_array ty then
                Param (size_t, TypeName.length_param ty) :: parameters
              else
                parameters
            in
            let parameters = Param (ty, name) :: parameters in
            Decl_Function (void, fname, parameters, error_list)

        | _ -> failwith @@ show_decl (SymbolTable.pp_symbol symtab) decl
      end

  | decl ->
      failwith @@ show_decl (SymbolTable.pp_symbol symtab) decl


let rec rename_symbols name symtab = function
  | Decl_Comment (comment, decl) ->
      rename_symbols name symtab decl

  | Decl_Function (_, fname, _, _) as decl ->
      let name = SymbolTable.name symtab name in

      begin match SymbolTable.name symtab fname with
        | "size" ->
            SymbolTable.rename symtab fname
              (fun _ -> "get_" ^ name ^ "_size")

        | "get" ->
            SymbolTable.rename symtab fname
              (fun _ -> "get_" ^ name)

        | "set" ->
            SymbolTable.rename symtab fname
              (fun _ -> "set_" ^ name)

        | _ ->
            failwith @@ show_decl (SymbolTable.pp_symbol symtab) decl
      end

  | decl ->
      failwith @@ show_decl (SymbolTable.pp_symbol symtab) decl


let fold_decl v state = function
  | Decl_GetSet (type_name, lname, decls) ->
      let symtab = ReplaceDecl.get state in

      let decls = List.rev_map (add_types symtab lname type_name) decls in

      let symtab = List.fold_left (rename_symbols lname) symtab decls in

      let state = ReplaceDecl.set state symtab in
      let state = ReplaceDecl.replace state decls in

      state, Decl_GetSet (type_name, lname, decls)

  | decl ->
      ReplaceDecl.fold_decl v state decl


let v = { default with fold_decl }


let transform (symtab, decls) =
  let state, decls = visit_decls v (ReplaceDecl.initial symtab) decls in
  ReplaceDecl.get state, decls
