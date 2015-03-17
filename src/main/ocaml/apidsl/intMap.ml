include Map.Make(struct

    type t = int

    let compare : t -> t -> int = compare

  end)


type pp_key = int [@@deriving show]

let pp_key = pp_pp_key

type 'a bindings = (key * 'a) list [@@deriving show]

let pp fmt pp_a map =
  pp_bindings fmt pp_a (bindings map)
