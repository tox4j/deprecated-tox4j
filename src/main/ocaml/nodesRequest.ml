open Core.Std
open Types


let kind = Packet.Kind.NodesRequest
type t = Packet.Data.nodes_request = {
  key : PublicKey.t;
  ping_id : int64;
}


let wrap decoded =
  Packet.Data.NodesRequest decoded


let unpack ~dht ~buf =
  DhtPacket.unpack ~dht ~buf
    ~f:(
      fun ~buf ->
        let key = PublicKey.unpack ~buf in
        let ping_id = Message.Consume.int64_t_be buf in
        Or_error.return { key; ping_id }
    )


let pack ~dht ~buf ~node ~packet =
  DhtPacket.pack ~dht ~buf ~node ~kind ~f:(
    fun ~buf ->
      PublicKey.pack ~buf packet.key;
      Message.Fill.int64_t_be buf packet.ping_id;
  )
