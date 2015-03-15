open Core.Std
open Types


let unpack ~dht ~buf ~f =
  let open Or_error in
  let public_key = PublicKey.unpack ~buf in
  let nonce      = Nonce.unpack ~buf in

  Dht.node_by_key ~dht public_key >>= fun node ->
  let channel_key = node.cn_ckey in
  Crypto.unpack_encrypted ~channel_key ~nonce buf >>= fun plain ->

  f ~buf:plain >>| fun result ->
  node, result


let pack ~dht ~buf ~node ~kind ~f =
  let channel_key = node.cn_ckey in
  let nonce = Nonce.random () in

  Packet.Kind.pack buf kind;

  PublicKey.pack buf dht.dht_pk;
  Nonce.pack buf nonce;

  Crypto.pack_encrypted ~channel_key ~nonce ~buf ~f
