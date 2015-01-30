open Core.Std
open Sodium


type t = Box.public_key


let compare = Box.compare_public_keys


let equal a b =
  compare a b = 0


let to_string_hum key =
  let key = Box.Bytes.of_public_key key in
  let buf = Buffer.create 64 in
  Bytes.iter
    (fun b ->
       Printf.bprintf buf "%02x" (Char.to_int b))
    key;
  Buffer.contents buf


let of_string s =
  Bytes.unsafe_of_string s
  |> Box.Bytes.to_public_key


let sexp_of_t pk =
  sexp_of_string @@ Bytes.to_string @@ Box.Bytes.of_public_key pk


let t_of_sexp sx =
  of_string @@ string_of_sexp sx


let pack ~buf public_key =
  Box.Bytes.of_public_key public_key
  |> Bytes.unsafe_to_string
  |> Iobuf.Fill.string buf


let unpack ~buf =
  Iobuf.Consume.string buf ~len:Box.public_key_size
  |> of_string


let distance a b =
  let a = Box.Bytes.of_public_key a |> Bytes.unsafe_to_string in
  let b = Box.Bytes.of_public_key b |> Bytes.unsafe_to_string in
  let dist = Bytes.create Box.public_key_size in

  for i = 0 to Box.public_key_size - 1 do
    Char.to_int a.[i] lxor Char.to_int b.[i]
    |> Char.of_int_exn
    |> Bytes.set dist i
  done;

  Bytes.unsafe_to_string dist
