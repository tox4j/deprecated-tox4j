open ApiAst

type t = string uname

let to_string : t -> string = Name.repr
let of_string : string -> t = Name.uname

let compare (a : t) (b : t) = String.compare (to_string a) (to_string b)

let prepend str name =
  Name.uname (str ^ "_" ^ to_string name)

let append name str =
  Name.uname (to_string name ^ "_" ^ str)
