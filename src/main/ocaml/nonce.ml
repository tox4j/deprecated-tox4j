open Core.Std
open Sodium


let random = Box.random_nonce


let pack ~buf nonce =
  Box.Bytes.of_nonce nonce
  |> Bytes.unsafe_to_string
  |> Message.Fill.string buf


let unpack ~buf =
  Message.Consume.string buf ~len:Box.nonce_size
  |> Bytes.unsafe_of_string
  |> Box.Bytes.to_nonce
