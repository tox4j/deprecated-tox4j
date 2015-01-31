open Core.Std


type t = private int

val min : t
val max : t

val of_int : int -> t option
val to_int : t -> int

val unpack : (Message.plain, t Or_error.t) Message.Consume.t
val pack   : (Message.plain, t) Message.Fill.t
