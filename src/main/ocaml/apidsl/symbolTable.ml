type scopes = string list [@@deriving show]


type scope = {
  name     : string;
  symbols  : int StringMap.t;
  children : scope StringMap.t;
} [@@deriving show]


type t = string IntMap.t * scope [@@deriving show]


let empty name = {
  name;
  symbols  = StringMap.empty;
  children = StringMap.empty;
}


let root = {
  (empty "<root>") with
  symbols  = (
    snd @@ List.fold_left
      (fun (id, symbols) sym ->
         id + 1, StringMap.add sym id symbols)
      (0, StringMap.empty)
      [
        "void";
        "bool";
        "uint8_t";
        "uint16_t";
        "uint32_t";
        "uint64_t";
        "size_t";
        "string";
      ]
  );
}


let enter_scope scope name =
  try
    StringMap.find name scope.children
  with Not_found ->
    empty name


let leave_scope scope child =
  { scope with
    children = StringMap.add child.name child scope.children }


let scoped scope name f x =
  let child = enter_scope scope name in
  let child = f child x in
  let scope = leave_scope scope child in
  scope


let scopedl scope lname f x =
  let name = LName.to_string lname in
  scoped scope name f x

let scopedu scope uname f x =
  let name = UName.to_string uname in
  scoped scope name f x


let add ?(extend=false) scope name =
  if StringMap.mem name scope.symbols then
    if not extend then
      failwith @@ "duplicate name: " ^ name
    else
      scope
  else
    let symbols =
      StringMap.add name (StringMap.cardinal scope.symbols) scope.symbols
    in
    { scope with symbols }


let addl ?extend lname scope =
  let name = LName.to_string lname in
  add ?extend scope name

let addu ?extend uname scope =
  let name = UName.to_string uname in
  add ?extend scope name


let rec assign_ids table scope =
  let symbols =
    let table_size = IntMap.cardinal table in
    StringMap.map (fun id -> id + table_size) scope.symbols
  in

  let table =
    StringMap.fold
      (fun name id table ->
         assert (not (IntMap.mem id table));
         IntMap.add id name table
      ) symbols table
  in

  let table, children =
    StringMap.fold
      (fun ns child (table, children) ->
         let table, child = assign_ids table child in
         table, StringMap.add ns child children
      ) scope.children (table, StringMap.empty)
  in

  table, { scope with symbols; children }


let make scope =
  assign_ids IntMap.empty scope


let lookup (_, root : t) scopes name =
  let scopes =
    List.fold_right
      (fun scope -> function
         | [] -> assert false
         | current :: scopes ->
             let scope = StringMap.find scope current.children in
             scope :: current :: scopes
      ) scopes [root]
  in

  let id =
    List.fold_left
      (fun id scope ->
         match id with
         | Some _ -> id
         | None ->
             try
               Some (StringMap.find name scope.symbols)
             with Not_found ->
               None
      ) None scopes
  in

  match id with
  | Some id -> id
  | None    -> -1


let uname (table, _) = function
  | -1 -> Name.uname "<UNRESOLVED>"
  | id -> Name.uname (IntMap.find id table)

let lname (table, _) = function
  | -1 -> Name.lname "<unresolved>"
  | id -> Name.lname (IntMap.find id table)
