open Core.Std
open Sodium


let precompute = Box.precompute
let random_keypair = Box.random_keypair


let decrypt ~channel_key ~nonce cipher_text =
  Or_error.try_with (fun () ->
      let cipher_text = Bytes.unsafe_of_string cipher_text in
      let plain_text = Box.Bytes.fast_box_open channel_key cipher_text nonce in
      Bytes.unsafe_to_string plain_text
    )


let encrypt ~channel_key ~nonce plain_text =
  let plain_text = Bytes.unsafe_of_string plain_text in
  let cipher_text = Box.Bytes.fast_box channel_key plain_text nonce in
  Bytes.unsafe_to_string cipher_text
