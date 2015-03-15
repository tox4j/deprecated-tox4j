open Sodium


type node = {
  n_proto : Protocol.t;
  n_addr  : InetAddr.t;
  n_port  : Port.t;
  n_key   : Box.public_key;
}


type connected_node = {
  cn_node : node;
  cn_ckey : Box.channel_key;
}


type dht = {
  dht_sk : Box.secret_key;
  dht_pk : Box.public_key;
  dht_nodes : connected_node PublicKeyMap.t;
}
