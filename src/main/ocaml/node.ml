open Core.Std


let unpack_after_proto ~buf n_proto n_addr =
  match Port.of_int (Iobuf.Consume.uint16_be buf) with
  | None ->
      Or_error.of_exn (Packet.Format_error "Node:port")
  | Some n_port ->
      let n_key = PublicKey.unpack ~buf in

      Or_error.return
        Types.({
          n_proto;
          n_addr;
          n_port;
          n_key;
        })


let unpack ~buf =
  let protocol = Iobuf.Consume.uint8 buf in

  let n_proto =
    match protocol lsr 7 with
    | 0 -> Protocol.UDP
    | 1 -> Protocol.TCP
    | _ -> assert false
  in

  match protocol land 0x7f with
  | 0b0010 ->
      unpack_after_proto ~buf n_proto
        (InetAddr.IPv4 (InetAddr.read_ipv4 buf))
  | 0b1010 ->
      unpack_after_proto ~buf n_proto
        (InetAddr.IPv6 (InetAddr.read_ipv6 buf))
  | _ ->
      Or_error.of_exn (Packet.Format_error "Node:protocol")


let pack ~buf node =
  assert false
