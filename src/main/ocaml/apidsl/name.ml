type ucase
type lcase

let pp_ucase _ _ = ()
let pp_lcase _ _ = ()

type ('case, 'id) name = 'id [@@deriving show]

type 'id uname = (ucase, 'id) name [@@deriving show]
type 'id lname = (lcase, 'id) name [@@deriving show]

let uname s = s
let lname s = s

let uid s = s
let lid s = s

let repr s = s
