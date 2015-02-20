open Core.Std


exception Range_error

type t = int

let min = 0
let max = 65535


let of_int i =
  if i < min || i > max then
    None
  else
    Some i


let to_int = Fn.id


let unpack buf =
  match of_int (Message.Consume.uint16_be buf) with
  | None ->
      Or_error.of_exn Range_error
  | Some port ->
      Or_error.return port


let pack = Message.Fill.uint16_be
