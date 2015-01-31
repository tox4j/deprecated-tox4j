open Core.Std


type ipv4
type ipv6

type 'kind address = private string

type t =
  | IPv4 of ipv4 address
  | IPv6 of ipv6 address

val ipv4_size : int
val ipv6_size : int

val ipv4_of_string : string -> ipv4 address option
val ipv6_of_string : string -> ipv6 address option

val read_ipv4 : (Message.plain, ipv4 address) Message.Consume.t
val read_ipv6 : (Message.plain, ipv6 address) Message.Consume.t

val write_ipv4 : (Message.plain, ipv4 address) Message.Fill.t
val write_ipv6 : (Message.plain, ipv6 address) Message.Fill.t

val ipv4_to_string : ipv4 address -> string
val ipv6_to_string : ipv6 address -> string

val to_string : t -> string
