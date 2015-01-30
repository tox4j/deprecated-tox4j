open Core.Std
open Types


let unpack dht packet ~f =
  let open Or_error in
  let public_key = PublicKey.unpack packet in
  let nonce      = Nonce.unpack packet in
  let data       = Iobuf.Consume.string packet in

  Dht.node_by_key dht public_key >>= fun node ->
  let channel_key = node.cn_ckey in
  Crypto.decrypt ~channel_key ~nonce data >>= fun plain ->

  f (Iobuf.of_string plain) >>| fun result ->
  node, result


let pack_encrypted ~channel_key ~nonce ~f packet =
  let plain = Iobuf.create ~len:Async.Std.Udp.default_capacity in
  f plain;
  Iobuf.flip_lo plain;

  let plain = Iobuf.to_string plain in

  Crypto.encrypt ~channel_key ~nonce plain
  |> Iobuf.Fill.string packet


let pack ~dht ~node ~kind ~f =
  let packet = Iobuf.create Async.Std.Udp.default_capacity in

  let channel_key = node.cn_ckey in
  let nonce = Nonce.random () in

  Iobuf.Fill.uint8 packet kind;

  PublicKey.pack packet dht.dht_pk;
  Nonce.pack packet nonce;

  pack_encrypted ~channel_key ~nonce ~f packet;

  Iobuf.flip_lo packet;
  packet
