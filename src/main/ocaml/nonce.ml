open Core.Std
open Sodium


let random = Box.random_nonce


let pack packet nonce =
  Box.Bytes.of_nonce nonce
  |> Bytes.unsafe_to_string
  |> Iobuf.Fill.string packet


let unpack packet =
  Iobuf.Consume.string packet ~len:Box.nonce_size
  |> Bytes.unsafe_of_string
  |> Box.Bytes.to_nonce
