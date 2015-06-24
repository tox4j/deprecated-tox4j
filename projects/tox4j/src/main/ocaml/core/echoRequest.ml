open Core.Std
open Types


let kind = Packet.Kind.EchoRequest
type t = Packet.Data.echo_request = {
  ping_id : int64;
}


let wrap decoded =
  Packet.Data.EchoRequest decoded


let unpack ~dht ~buf =
  DhtPacket.unpack ~dht ~buf
    ~f:(
      fun ~buf ->
        Or_error.return { ping_id = Message.Consume.int64_t_be buf }
    )


let pack ~dht ~buf ~node ~packet =
  DhtPacket.pack ~dht ~buf ~node ~kind ~f:(
    fun ~buf ->
      Message.Fill.int64_t_be buf packet.ping_id
  )
