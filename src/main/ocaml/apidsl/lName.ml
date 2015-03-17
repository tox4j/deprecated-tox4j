open ApiAst

type t = string lname

let to_string : t -> string = Name.repr
let of_string : string -> t = Name.lname

let compare (a : t) (b : t) = String.compare (to_string a) (to_string b)

let prepend str name =
  Name.lname (str ^ "_" ^ to_string name)

let append name str =
  Name.lname (to_string name ^ "_" ^ str)
