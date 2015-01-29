open Core.Std
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

  f (Iobuf.of_string (Bytes.unsafe_to_string plain)) >>| fun result ->
  node, result


let encrypt ~channel_key ~nonce ~f packet =
  let plain = Iobuf.create ~len:Async.Std.Udp.default_capacity in
  f plain;
  Iobuf.flip_lo plain;

  let plain = Bytes.unsafe_of_string (Iobuf.to_string plain) in

  Sodium.Box.Bytes.fast_box channel_key plain nonce
  |> Bytes.unsafe_to_string
  |> Iobuf.Fill.string packet


let pack_dht_packet ~dht ~node ~kind ~f =
  let open Option in
  PublicKeyMap.find dht.dht_nodes node.n_key >>| fun dht_node ->
  let packet = Iobuf.create Async.Std.Udp.default_capacity in

  let channel_key = dht_node.cn_ckey in
  let nonce = Nonce.random () in

  Iobuf.Fill.uint8 packet kind;

  PublicKey.pack packet dht.dht_pk;
  Nonce.pack packet nonce;

  encrypt ~channel_key ~nonce ~f packet;

  Iobuf.flip_lo packet;
  packet
