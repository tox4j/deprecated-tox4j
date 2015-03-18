open ApiAst
open ApiMap


let map_decl v state = function

  | Decl_Static (Decl_Function _) as decl ->
      decl

  | Decl_Function (type_name, lname, parameters, error_list) ->
      let parameters = Param (TypeName.this, "tox") :: parameters in
      Decl_Function (type_name, lname, parameters, error_list)

  | decl ->
      visit_decl v state decl


let v = { default with map_decl }


let transform decls =
  visit_decls v () decls
