open ApiAst
open ApiFold


type 'id ns =
  | Normal of 'id lname
  | Inline of 'id lname


let ns_map f = function
  | Normal n -> Normal (f n)
  | Inline n -> Inline (f n)


let prepend_ns ~ignore_inline ns name =
  List.fold_left
    (fun name ns ->
       match ns with
       | Normal ns ->
           ns ^ "_" ^ name
       | Inline ns ->
           if ignore_inline then
             name
           else
             ns ^ "_" ^ name
    ) name ns


let resolve_ns symtab ns =
  List.map (ns_map (SymbolTable.name symtab)) ns


let fold_namespace v (symtab, ignore_first, ns) name decls =
  let symtab, _, _ =
    if ignore_first = 0 then
      visit_list v.fold_decl v (symtab, ignore_first, name :: ns) decls
    else
      visit_list v.fold_decl v (symtab, ignore_first - 1, ns) decls
  in
  (symtab, ignore_first, ns)


let fold_decl v (symtab, ignore_first, ns) = function
  | Decl_Error (uname, _)
  | Decl_Enum (_, uname, _)
  | Decl_Const (uname, _) ->
      let symtab =
        resolve_ns symtab ns
        |> List.map (ns_map String.uppercase)
        |> prepend_ns ~ignore_inline:true
        |> SymbolTable.rename symtab uname
      in

      (symtab, ignore_first, ns)

  | Decl_Struct (lname, decls) ->
      (* Reset namespace for struct members. *)
      let symtab, _, _ = visit_list v.fold_decl v (symtab, ignore_first, []) decls in
      (symtab, ignore_first, ns)

  | Decl_Typedef (_, lname, _)
  | Decl_Member (_, lname)
  | Decl_Function (_, lname, _, _) ->
      let symtab =
        resolve_ns symtab ns
        |> prepend_ns ~ignore_inline:false
        |> SymbolTable.rename symtab lname
      in

      (symtab, ignore_first, ns)

  | Decl_Inline (Decl_Namespace (name, decls)) ->
      fold_namespace v (symtab, ignore_first, ns) (Inline name) decls

  | Decl_Namespace (name, decls) ->
      fold_namespace v (symtab, ignore_first, ns) (Normal name) decls

  | decl ->
      visit_decl v (symtab, ignore_first, ns) decl


let v = { default with fold_decl }


let transform ignore_first (symtab, decls) =
  let symtab, _, _ =
    visit_decls v (symtab, ignore_first, []) decls
  in
  symtab, decls
