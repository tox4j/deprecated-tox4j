type t

val of_bytes : bytes -> t
val of_string : string -> t

val read : t -> t * bytes

val read_uint1 : t -> t * int
val read_uint7 : t -> t * int
val read_uint8 : t -> t * int
val read_uint16 : t -> t * int
val read_uint32 : t -> t * int32
val read_uint64 : t -> t * int64
val read_bytes : t -> int -> t * bytes
val read_string : t -> int -> t * string
