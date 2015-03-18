open ApiAst
open ApiMap


let map_decl v this_name = function

  | Decl_Static (Decl_Namespace _)
  | Decl_Static (Decl_Function _) as decl ->
      decl

  | Decl_Function (type_name, lname, parameters, error_list) ->
      let parameters = Param (TypeName.this, this_name) :: parameters in
      Decl_Function (type_name, lname, parameters, error_list)

  | Decl_Class (lname, decls) ->
      let decls = visit_list v.map_decl v lname decls in
      Decl_Class (lname, decls)

  | decl ->
      visit_decl v this_name decl


let v = { default with map_decl }


let transform decls =
  visit_decls v "" decls
