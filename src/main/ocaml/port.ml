type t = int

let min = 0
let max = 65535


let of_int i =
  if i < min || i > max then
    None
  else
    Some i

let to_int i = i
