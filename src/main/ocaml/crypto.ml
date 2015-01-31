open Core.Std
open Sodium


let precompute = Box.precompute
let random_keypair = Box.random_keypair


let fast_box_open ~channel_key ~nonce cipher_text =
  Or_error.try_with
    (fun () -> Box.Bytes.fast_box_open channel_key cipher_text nonce)

let decrypt ~channel_key ~nonce cipher_text =
  let open Or_error in
  cipher_text
  |> Message.Consume.string
  |> Bytes.unsafe_of_string
  |> fast_box_open ~channel_key ~nonce
  >>| fun plain_text ->
  plain_text
  |> Bytes.unsafe_to_string

let unpack_encrypted ~channel_key ~nonce cipher_text =
  let open Or_error in
  decrypt ~channel_key ~nonce cipher_text
  >>| Message.plain_of_string


let fast_box ~channel_key ~nonce plain_text =
  Box.Bytes.fast_box channel_key plain_text nonce

let encrypt ~channel_key ~nonce plain_text =
  plain_text
  |> Message.to_string
  |> Bytes.unsafe_of_string
  |> fast_box ~channel_key ~nonce
  |> Bytes.unsafe_to_string

let pack_encrypted ~channel_key ~nonce ~buf ~f =
  let plain = Message.plain () in
  f ~buf:plain;
  Message.flip_lo plain;

  encrypt ~channel_key ~nonce plain
  |> Message.Fill.string buf
