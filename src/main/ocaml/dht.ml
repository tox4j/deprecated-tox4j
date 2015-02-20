open Core.Std
open Types


type t = Types.dht


let create () =
  let dht_sk, dht_pk = Crypto.random_keypair () in
  let dht_nodes = PublicKeyMap.empty in
  { dht_sk; dht_pk; dht_nodes; }


let add_dht_node ~dht dht_node =
  { dht with
    dht_nodes =
      PublicKeyMap.add dht.dht_nodes
        ~key:dht_node.cn_node.n_key
        ~data:dht_node
  }


let add_node ~dht node =
  match PublicKeyMap.find dht.dht_nodes node.n_key with
  | None ->
      add_dht_node ~dht {
        cn_node = node;
        cn_ckey = Crypto.precompute dht.dht_sk node.n_key;
      }
  | Some dht_node ->
      assert (PublicKey.equal node.n_key dht_node.cn_node.n_key);
      if dht_node.cn_node = node then
        dht
      else
        add_dht_node ~dht {
          dht_node with
          cn_node = node;
        }


exception No_such_key

let node_by_key ~dht public_key =
  match PublicKeyMap.find dht.dht_nodes public_key with
  | None ->
      Or_error.of_exn No_such_key
  | Some node ->
      Or_error.return node
