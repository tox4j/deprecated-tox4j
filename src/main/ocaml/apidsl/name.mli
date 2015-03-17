type ucase
type lcase

type ('case, 'id) name = private 'id

type 'id uname = (ucase, 'id) name [@@deriving show]
type 'id lname = (lcase, 'id) name [@@deriving show]

val uname : string -> string uname
val lname : string -> string lname
