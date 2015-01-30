open Core.Std
open Types


type t = {
  ping_id : int64;
}


let unpack dht packet =
  let packet_id = Iobuf.Consume.uint8 packet in
  assert (packet_id = 0x00);

  DhtPacket.unpack dht packet
    ~f:(
      fun packet ->
        Or_error.return { ping_id = Iobuf.Consume.int64_t_be packet }
    )
