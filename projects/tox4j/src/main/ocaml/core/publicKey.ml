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


let (%%) = Fn.compose


let to_string =
  Bytes.unsafe_to_string %% Box.Bytes.of_public_key


let of_string =
  Box.Bytes.to_public_key %% Bytes.unsafe_of_string


let sexp_of_t =
  sexp_of_string %% Bytes.to_string %% Box.Bytes.of_public_key


let t_of_sexp =
  of_string %% string_of_sexp


let pack ~buf =
  Message.Fill.string buf %% Bytes.unsafe_to_string %% Box.Bytes.of_public_key


let unpack ~buf =
  of_string (Message.Consume.string ~len:Box.public_key_size buf)


let kad_distance dist a b =
  for i = 0 to Box.public_key_size - 1 do
    Char.to_int a.[i] lxor Char.to_int b.[i]
    |> Char.of_int_exn
    |> Bytes.set dist i
  done


let tox_distance dist a b =
  let int8_t i =
    if i >= 128 then
      -(256 - i)
    else
      i
  in

  for i = 0 to Box.public_key_size - 1 do
    abs (int8_t (Char.to_int a.[i]) lxor int8_t (Char.to_int b.[i]))
    |> Char.of_int_exn
    |> Bytes.set dist i
  done


let use_kad_distance = false

let distance a b =
  let a = to_string a in
  let b = to_string b in
  let dist = Bytes.create Box.public_key_size in

  if use_kad_distance then
    kad_distance dist a b
  else
    tox_distance dist a b;

  Bytes.unsafe_to_string dist
