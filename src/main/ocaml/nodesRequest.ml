open Core.Std
open Types


let kind = 0x02

type t = {
  key : PublicKey.t;
  ping_id : int64;
}


let make ~node request =
  DhtPacket.pack ~node ~kind ~f:(
    fun packet ->
      PublicKey.pack packet request.key;
      Iobuf.Fill.int64_t_be packet request.ping_id;
  )
