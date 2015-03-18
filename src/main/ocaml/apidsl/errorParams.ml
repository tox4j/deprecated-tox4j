open ApiAst
open ApiMap


let map_decl v symtab = function

  | Decl_Function (type_name, lname, parameters, Err_From error_type_name) ->
      let type_name = v.map_type_name v symtab type_name in
      let lname = v.map_lname v symtab lname in
      let parameters =
        let error =
          let error_ty = Ty_Pointer (Ty_UName error_type_name) in
          let error_name = SymbolTable.lookup symtab [] "error" in
          Param (error_ty, error_name)
        in
        parameters @ [error]
      in
      Decl_Function (type_name, lname, parameters, Err_None)

  | decl ->
      visit_decl v symtab decl


let v = { default with map_decl }


let transform (symtab, decls) =
  symtab, visit_decls v symtab decls
