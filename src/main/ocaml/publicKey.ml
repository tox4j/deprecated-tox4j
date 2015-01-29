open Core.Std
open Sodium


type t = Box.public_key


let compare = Box.compare_public_keys


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


let pack packet public_key =
  Box.Bytes.of_public_key public_key
  |> Bytes.unsafe_to_string
  |> Iobuf.Fill.string packet


let unpack packet =
  Iobuf.Consume.string packet ~len:Box.public_key_size
  |> of_string
