include Map.Make(String)

type pp_key = string [@@deriving show]

let pp_key = pp_pp_key

type 'a bindings = (key * 'a) list [@@deriving show]

let pp fmt pp_a map =
  pp_bindings fmt pp_a (bindings map)
