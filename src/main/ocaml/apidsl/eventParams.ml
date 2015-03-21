open ApiAst
open ApiMap


let map_decl v symtab = function

  | Decl_Event (event_name, [
      Decl_Comment (comment,
                    Decl_Typedef (cb_return, cb_type_name, cb_params));
      Decl_Function (type_name, fname, parameters, error_list);
    ]) ->
      let user_data =
        let void = SymbolTable.lookup symtab [] "void" in
        let cb_type = Ty_Pointer (Ty_LName void) in
        let cb_name = SymbolTable.lookup symtab [] "user_data" in
        Param (cb_type, cb_name)
      in

      let callback =
        let cb_type = Ty_Pointer (Ty_LName cb_type_name) in
        let cb_name = SymbolTable.lookup symtab [] "callback" in
        Param (cb_type, cb_name)
      in

      let cb_params = cb_params @ [user_data] in
      let parameters = parameters @ [callback; user_data] in

      Decl_Event (event_name, [
          Decl_Comment (comment,
                        Decl_Typedef (cb_return, cb_type_name, cb_params));
          Decl_Function (type_name, fname, parameters, error_list);
        ])

  | decl ->
      visit_decl v symtab decl


let v = { default with map_decl }


let transform (symtab, decls) =
  symtab, visit_decls v symtab decls
