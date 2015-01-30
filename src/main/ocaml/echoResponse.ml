open Core.Std
open Types


let kind = 0x01

type t = {
  ping_id : int64;
}


let make ~dht ~node request =
  DhtPacket.pack ~dht ~node ~kind ~f:(
    fun packet ->
      Iobuf.Fill.int64_t_be packet request.ping_id;
  )
