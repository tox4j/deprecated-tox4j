type t

val create : int -> t

val to_bytes : t -> bytes

val add_uint1 : t -> int -> unit
val add_uint7 : t -> int -> unit
val add_uint8 : t -> int -> unit
val add_uint16 : t -> int -> unit
val add_uint32 : t -> int32 -> unit
val add_uint64 : t -> int64 -> unit
val add_bytes : t -> bytes -> int -> unit
val add_string : t -> bytes -> int -> unit
