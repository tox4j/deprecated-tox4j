open Core.Std


let unpack packet =
  let open Option in
  let protocol = Iobuf.Consume.uint8 packet in

  let n_proto =
    match protocol lsr 7 with
    | 0 -> Protocol.UDP
    | 1 -> Protocol.TCP
    | _ -> assert false
  in

  (match protocol land 0x7f with
   | 0b0010 -> Some (InetAddr.IPv4 (InetAddr.read_ipv4 packet))
   | 0b1010 -> Some (InetAddr.IPv6 (InetAddr.read_ipv6 packet))
   | _ -> None) >>= fun n_addr ->

  Port.of_int (Iobuf.Consume.uint16_be packet) >>| fun n_port ->

  let n_key = PublicKey.unpack packet in

  Types.({
    n_proto;
    n_addr;
    n_port;
    n_key;
  })
