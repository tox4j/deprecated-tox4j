open ApiAst


let prepend_ns ns name =
  List.fold_left
    (fun (LName name) (LName ns) ->
       LName (ns ^ "_" ^ name)
    ) name ns


let transform decls =
  let open ApiMap in

  let map_function_name v ns = function
    | Fn_Custom (type_name, lname) ->
        let type_name = v.map_type_name v ns type_name in
        let lname = prepend_ns ns lname in
        Fn_Custom (type_name, lname)

    | function_name ->
        visit_function_name v ns function_name
  in

  let map_decl v ns = function
    | Decl_Namespace (name, decls) ->
        let ns' = name :: ns in
        let decls = visit_list v.map_decl v ns' decls in
        Decl_Namespace (name, decls)

    | decl ->
        visit_decl v ns decl
  in

  let v = {
    default with
    map_function_name;
    map_decl;
  } in
  visit_decls v [] decls
