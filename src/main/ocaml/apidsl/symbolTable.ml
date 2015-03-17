type scope = {
  name     : string;
  symbols  : int StringMap.t;
  children : scope StringMap.t;
} [@@deriving show]


type t = string IntMap.t * scope [@@deriving show]


let empty = {
  name     = "<root>";
  symbols  = StringMap.empty;
  children = StringMap.empty;
}


let enter_scope scope name =
  try
    StringMap.find name scope.children
  with Not_found ->
    { empty with name }


let leave_scope scope child =
  { scope with
    children = StringMap.add child.name child scope.children }


let scoped scope name f x =
  let child = enter_scope scope name in
  let child, x = f child x in
  let scope = leave_scope scope child in
  scope, x


let scopedl scope lname f x =
  let name = LName.to_string lname in
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


let addl ?extend scope lname =
  let name = LName.to_string lname in
  add ?extend scope name


let rec assign_ids table scope =
  let symbols =
    StringMap.map (fun id -> id + IntMap.cardinal table) scope.symbols
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
