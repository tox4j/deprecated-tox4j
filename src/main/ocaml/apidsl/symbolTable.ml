type tree = {
  name : string;
  scope : int StringMap.t;
  children : tree StringMap.t;
} [@@deriving show]


type t = (string IntMap.t * tree) [@@deriving show]


let empty = {
  name = "<root>";
  scope = StringMap.empty;
  children = StringMap.empty;
}


let enter_scope tree name =
  try
    StringMap.find name tree.children
  with Not_found ->
    { empty with name }


let leave_scope tree scope =
  { tree with
    children = StringMap.add scope.name scope tree.children }


let scoped tree name f x =
  let scope = enter_scope tree name in
  let scope, x = f scope x in
  let tree = leave_scope tree scope in
  tree, x


let scopedl tree lname f x =
  let name = LName.to_string lname in
  scoped tree name f x


let add tree name =
  if StringMap.mem name tree.scope then
    failwith @@ "duplicate name: " ^ name;

  let scope = StringMap.add name (StringMap.cardinal tree.scope) tree.scope in
  { tree with scope }


let addl tree lname =
  let name = LName.to_string lname in
  add tree name


let rec assign_ids table tree =
  let scope =
    StringMap.map (fun id -> id + IntMap.cardinal table) tree.scope
  in

  let table =
    StringMap.fold
      (fun name id table ->
         assert (not (IntMap.mem id table));
         IntMap.add id name table
      ) scope table
  in

  let table, children =
    StringMap.fold
      (fun ns child (table, children) ->
         let table, child = assign_ids table child in
         table, StringMap.add ns child children
      ) tree.children (table, StringMap.empty)
  in

  table, { tree with scope; children }


let make tree =
  assign_ids IntMap.empty tree
