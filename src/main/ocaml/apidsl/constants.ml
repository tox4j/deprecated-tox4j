open ApiAst


let transform decls =
  let open ApiMap in

  let map_decl v state = function
    | Decl_Const (name, expr) ->
        let macro =
          Macro (
            Format.asprintf "#define %-30s %a"
              name
              ApiCodegen.cg_expr expr
          )
        in
        Decl_Macro macro

    | decl ->
        visit_decl v state decl
  in

  let v = { default with map_decl } in
  visit_decls v () decls
