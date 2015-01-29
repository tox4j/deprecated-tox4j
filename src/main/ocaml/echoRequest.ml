open Core.Std
open Types


type t = {
  ping_id : int64;
}


let unpack dht packet =
  let open Option in

  let packet_id = Iobuf.Consume.uint8 packet in
  assert (packet_id = 0x00);

  Crypto.unpack_dht_packet dht packet
    ~f:(
      fun packet ->
        return { ping_id = Iobuf.Consume.int64_t_be packet }
    )
