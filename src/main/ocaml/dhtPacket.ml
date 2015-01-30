open Core.Std
open Types


let unpack ~dht ~buf ~f =
  let open Or_error in
  let public_key = PublicKey.unpack ~buf in
  let nonce      = Nonce.unpack ~buf in
  let data       = Iobuf.Consume.string buf in

  Dht.node_by_key ~dht public_key >>= fun node ->
  let channel_key = node.cn_ckey in
  Crypto.decrypt ~channel_key ~nonce data >>= fun plain ->

  f ~buf:(Iobuf.of_string plain) >>| fun result ->
  node, result


let pack_encrypted ~channel_key ~nonce ~f ~buf =
  let plain = Iobuf.create Async.Std.Udp.default_capacity in
  f ~buf:plain;
  Iobuf.flip_lo plain;

  let plain = Iobuf.to_string plain in

  Crypto.encrypt ~channel_key ~nonce plain
  |> Iobuf.Fill.string buf


let pack ~dht ~buf ~node ~kind ~f =
  let channel_key = node.cn_ckey in
  let nonce = Nonce.random () in

  Packet.Kind.pack buf kind;

  PublicKey.pack buf dht.dht_pk;
  Nonce.pack buf nonce;

  pack_encrypted ~channel_key ~nonce ~f ~buf
