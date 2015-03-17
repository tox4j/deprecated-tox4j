type tree = {
  name : string;
  scope : int StringMap.t;
  children : tree StringMap.t;
} [@@deriving show]


type t = (tree * string IntMap.t) [@@deriving show]


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

  { tree with scope = StringMap.add name 0 tree.scope }


let addl tree lname =
  let name = LName.to_string lname in
  add tree name


let make tree =
  (tree, IntMap.empty)
