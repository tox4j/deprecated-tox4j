open Core.Std
open Types


type t = Types.dht


let create () =
  let dht_sk, dht_pk = Crypto.random_keypair () in
  let dht_nodes = PublicKeyMap.empty in
  { dht_sk; dht_pk; dht_nodes; }


let add_node dht node =
  assert (not (PublicKeyMap.mem dht.dht_nodes node.n_key));
  let dht_node = {
    cn_node = node;
    cn_ckey = Crypto.precompute dht.dht_sk node.n_key;
  } in

  { dht with
    dht_nodes =
      PublicKeyMap.add dht.dht_nodes
        ~key:node.n_key
        ~data:dht_node
  }
