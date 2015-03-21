open ApiAst
open ApiFoldMap


let fold_enumerator v state = function
  | Enum_Name _ as enumerator ->
      enumerator :: state, enumerator

  | enumerator ->
      visit_enumerator v state enumerator


let fold_decl v state = function
  | Decl_Enum (is_class, lname, _) as enum ->
      let state, _ = visit_decl v state enum in
      let state = List.rev state in

      [], Decl_Enum (is_class, lname, state)

  | decl ->
      visit_decl v state decl


let v = {
  default with
  fold_enumerator;
  fold_decl;
}


let transform (symtab, decls) =
  symtab, snd @@ visit_decls v ([]) decls
