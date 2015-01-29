open Core.Std
open Types


type t = {
  ping_id : int64;
}


let make ~dht ~node request =
  Crypto.pack_dht_packet ~dht ~node ~kind:0x01 ~f:(
    fun packet ->
      Iobuf.Fill.int64_t_be packet request.ping_id;
  )
