open ApiAst


module SymbolTable = struct

  type t = {
    ids : (string, int) Hashtbl.t;
    names : (int, string) Hashtbl.t;
  }

  let create init = {
    ids = Hashtbl.create init;
    names = Hashtbl.create init;
  }

  let add symtab name =
    try
      Hashtbl.find symtab.ids name
    with Not_found ->
      let id = Hashtbl.length symtab.ids in
      Hashtbl.add symtab.ids name id;
      Hashtbl.add symtab.names id name;
      id

  let name symtab id =
    Hashtbl.find symtab.names id

end


let transform decls =
  let open ApiFold in

  let fold_name v symtab name =
    let id = SymbolTable.add symtab name in
    symtab, id
  in

  let fold_decl v symtab = function
    | decl ->
        ApiFold.visit_decl v symtab decl
  in

  let v = ApiFold.make
    ~fold_name
    ~fold_decl
    ()
  in
  visit_decls v (SymbolTable.create 11) decls


let inverse (symtab, decls) =
  let open ApiMap in

  let map_name v symtab name =
    SymbolTable.name symtab name ^ "'" ^ string_of_int name
  in

  let v = ApiMap.make
    ~map_name
    ()
  in
  visit_decls v symtab decls
