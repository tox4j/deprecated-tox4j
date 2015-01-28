open Core.Std
open Async.Std
open Types


let precompute = Sodium.Box.precompute
let random_keypair = Sodium.Box.random_keypair


let unpack_dht_packet dht packet ~f =
  let open Option in
  let public_key = PublicKey.unpack packet in
  let nonce      = Nonce.unpack packet in
  let data       = Bytes.unsafe_of_string (Iobuf.Consume.string packet) in

  PublicKeyMap.find dht.dht_nodes public_key >>= fun node ->
  let channel_key = node.cn_ckey in
  let plain = Sodium.Box.Bytes.fast_box_open channel_key data nonce in

  f (Iobuf.of_string (Bytes.unsafe_to_string plain))


let pack ~channel_key ~nonce ~f packet =
  let plain = Iobuf.create ~len:Udp.default_capacity in
  f plain;
  Iobuf.flip_lo plain;

  let plain = Bytes.unsafe_of_string (Iobuf.to_string plain) in

  Sodium.Box.Bytes.fast_box channel_key plain nonce
  |> Bytes.unsafe_to_string
  |> Iobuf.Fill.string packet
