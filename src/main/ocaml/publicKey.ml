open Core.Std

open Sodium

type t = Box.public_key

let compare = Box.compare_public_keys

let sexp_of_t pk =
  sexp_of_string @@ Bytes.to_string @@ Box.Bytes.of_public_key pk

let t_of_sexp sx =
  Box.Bytes.to_public_key @@ Bytes.of_string @@ string_of_sexp sx
