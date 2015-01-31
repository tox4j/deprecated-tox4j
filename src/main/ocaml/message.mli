open Core.Std


type cipher
type plain

type ('encryption, 'access, 'seek) t


val cipher : unit -> (cipher, 'b, 'c) t
val plain  : unit -> (plain , 'b, 'c) t

val cipher_of_string : string -> (cipher, 'b, 'c) t
val plain_of_string  : string -> (plain , 'b, 'c) t

val of_iobuf
  :  ('access, Iobuf.seek) Iobuf.t
  -> (cipher, read_only, Iobuf.seek) t

val to_iobuf
  :  (cipher, 'access, Iobuf.seek) t
  -> (read_only, Iobuf.seek) Iobuf.t

val flip_lo : ('a, 'b, Iobuf.seek) t -> unit
val to_string : ?len:int -> ('a, 'b, 'c) t -> string


module type Accessor = sig
  type ('encryption, 'a) t

  val uint8      :  ('encryption, int   ) t
  val uint16_be  :  ('encryption, int   ) t
  val int64_t_be :  ('encryption, int64 ) t
  val string     :  ?str_pos:int -> ?len:int
                 -> ('encryption, string) t
end

module Consume : sig
  type 'encryption src = ('encryption, read_only, Iobuf.seek) t

  include Accessor with
    type ('encryption, 'a) t = 'encryption src -> 'a
end


module Fill : sig
  type 'encryption src = ('encryption, read_write, Iobuf.seek) t

  include Accessor with
    type ('encryption, 'a) t = 'encryption src -> 'a -> unit
end
