open Core.Std
open Types


let kind = Packet.Kind.NodesResponse
type t = Packet.Data.nodes_response = {
  nodes   : node list;
  ping_id : int64;
}


let wrap decoded =
  Packet.Data.NodesResponse decoded


let unpack ~dht ~buf =
  let open Or_error in

  DhtPacket.unpack ~dht ~buf
    ~f:(
      fun ~buf ->
        Packet.unpack_repeated ~buf ~size:Message.Consume.uint8 ~f:Node.unpack
        >>| fun nodes ->
        let ping_id = Message.Consume.int64_t_be buf in
        {
          nodes;
          ping_id;
        }
    )


let pack ~dht ~buf ~node ~packet =
  DhtPacket.pack ~dht ~buf ~node ~kind ~f:(
    fun ~buf ->
      Packet.pack_repeated
        ~buf ~size:Message.Fill.uint8 packet.nodes ~f:Node.pack;
      Message.Fill.int64_t_be buf packet.ping_id;
  )
