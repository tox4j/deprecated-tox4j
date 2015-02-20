open Core.Std


let unpack_after_proto ~buf n_proto n_addr =
  let open Or_error in
  Port.unpack buf >>= fun n_port ->
  let n_key = PublicKey.unpack ~buf in

  return
    Types.({
        n_proto;
        n_addr;
        n_port;
        n_key;
      })


let unpack ~buf =
  let protocol = Message.Consume.uint8 buf in

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
