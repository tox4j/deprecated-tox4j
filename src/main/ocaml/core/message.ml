open Core.Std


type cipher
type plain

type ('encryption, 'access, 'seek) t = ('access, 'seek) Iobuf.t


let cipher () = Iobuf.create Async.Std.Udp.default_capacity
let plain  () = Iobuf.create Async.Std.Udp.default_capacity

let cipher_of_string = Iobuf.of_string
let plain_of_string  = Iobuf.of_string

let of_iobuf = Iobuf.read_only
let to_iobuf = Iobuf.read_only

let flip_lo = Iobuf.flip_lo

let to_string = Iobuf.to_string


module type Accessor = sig
  type ('encryption, 'a) t

  val uint8      :  ('encryption, int   ) t
  val uint16_be  :  ('encryption, int   ) t
  val int64_t_be :  ('encryption, int64 ) t
  val string     :  ?str_pos:int -> ?len:int
                 -> ('encryption, string) t
end


module Consume = struct
  type 'encryption src = ('encryption, read_only, Iobuf.seek) t
  type ('encryption, 'a) t = 'encryption src -> 'a

  open Iobuf.Consume
  let uint8      = uint8
  let uint16_be  = uint16_be
  let int64_t_be = int64_t_be
  let string     = string
end


module Fill = struct
  type 'encryption src = ('encryption, read_write, Iobuf.seek) t
  type ('encryption, 'a) t = 'encryption src -> 'a -> unit

  open Iobuf.Fill
  let uint8      = uint8
  let uint16_be  = uint16_be
  let int64_t_be = int64_t_be
  let string     = string
end
