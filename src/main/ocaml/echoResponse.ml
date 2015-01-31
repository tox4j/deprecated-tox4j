open Core.Std
open Types


let kind = Packet.Kind.EchoResponse
type t = Packet.Data.echo_response = {
  ping_id : int64;
}


let wrap decoded =
  Packet.Data.EchoResponse decoded


let unpack ~dht ~buf =
  DhtPacket.unpack ~dht ~buf
    ~f:(
      fun ~buf ->
        Or_error.return { ping_id = Message.Consume.int64_t_be buf }
    )


let pack ~dht ~buf ~node ~packet =
  DhtPacket.pack ~dht ~buf ~node ~kind ~f:(
    fun ~buf ->
      Message.Fill.int64_t_be buf packet.ping_id;
  )
