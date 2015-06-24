open Core.Std


type ipv4
type ipv6

type 'kind address = string

type t =
  | IPv4 of ipv4 address
  | IPv6 of ipv6 address


let ipv4_size = 4
let ipv6_size = 16


let ipv4_of_string s =
  if String.length s <> ipv4_size then
    None
  else
    Some s


let ipv6_of_string s =
  if String.length s <> ipv6_size then
    None
  else
    Some s


let read_ipv4 buf =
  Message.Consume.string ~len:ipv4_size buf

let read_ipv6 buf =
  Message.Consume.string ~len:ipv6_size buf


let write_ipv4 buf ip =
  Message.Fill.string buf ip

let write_ipv6 buf ip =
  Message.Fill.string buf ip


let ipv4_to_string ip =
  Printf.sprintf "%d.%d.%d.%d"
    (Char.to_int ip.[0])
    (Char.to_int ip.[1])
    (Char.to_int ip.[2])
    (Char.to_int ip.[3])


let ipv6_to_string ip =
  Printf.sprintf "%x%x:%x%x:%x%x:%x%x:%x%x:%x%x:%x%x:%x%x"
    (Char.to_int ip.[0])
    (Char.to_int ip.[1])
    (Char.to_int ip.[2])
    (Char.to_int ip.[3])
    (Char.to_int ip.[4])
    (Char.to_int ip.[5])
    (Char.to_int ip.[6])
    (Char.to_int ip.[7])
    (Char.to_int ip.[8])
    (Char.to_int ip.[9])
    (Char.to_int ip.[10])
    (Char.to_int ip.[11])
    (Char.to_int ip.[12])
    (Char.to_int ip.[13])
    (Char.to_int ip.[14])
    (Char.to_int ip.[15])


let to_string = function
  | IPv4 ip -> ipv4_to_string ip
  | IPv6 ip -> ipv6_to_string ip
