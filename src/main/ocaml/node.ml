open Core.Std


let unpack_after_proto packet n_proto n_addr =
  match Port.of_int (Iobuf.Consume.uint16_be packet) with
  | None ->
      Or_error.of_exn Packet.Format_error
  | Some n_port ->
      let n_key = PublicKey.unpack packet in

      Or_error.return
        Types.({
          n_proto;
          n_addr;
          n_port;
          n_key;
        })


let unpack packet =
  let protocol = Iobuf.Consume.uint8 packet in

  let n_proto =
    match protocol lsr 7 with
    | 0 -> Protocol.UDP
    | 1 -> Protocol.TCP
    | _ -> assert false
  in

  match protocol land 0x7f with
  | 0b0010 ->
      unpack_after_proto packet n_proto
        (InetAddr.IPv4 (InetAddr.read_ipv4 packet))
  | 0b1010 ->
      unpack_after_proto packet n_proto
        (InetAddr.IPv6 (InetAddr.read_ipv6 packet))
  | _ ->
      Or_error.of_exn Packet.Format_error
