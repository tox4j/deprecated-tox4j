open Core.Std
open Types


type t = {
  nodes   : node list;
  ping_id : int64;
}


let unpack dht packet =
  let open Option in

  let packet_id = Iobuf.Consume.uint8 packet in
  assert (packet_id = 0x04);

  Crypto.unpack_dht_packet dht packet
    ~f:(
      fun packet ->
        Packet.unpack_repeated packet ~size:Iobuf.Consume.uint8 ~f:Node.unpack
        >>| fun nodes ->
        let ping_id = Iobuf.Consume.int64_t_be packet in
        {
          nodes;
          ping_id;
        }
    )
