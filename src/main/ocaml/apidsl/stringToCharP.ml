open ApiAst
open ApiMap


let map_type_name v state = function

  | Ty_LName "string" ->
      Ty_Const (Ty_Pointer (Ty_LName "char"))

  | type_name ->
      visit_type_name v state type_name


let v = { default with map_type_name }


let transform decls =
  visit_decls v () decls
