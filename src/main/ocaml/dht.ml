open Core.Std

open Sodium


module Node_addr = struct
  type t = {
    ip   : string;
    port : int;
    key  : Box.public_key;
  }


  let sonOfRa = {
    ip = "144.76.60.215";
    port = 33445;
    key =
      Box.Bytes.to_public_key (
        Bytes.of_string (
          "\x04\x11\x9E\x83\x5D\xF3\xE7\x8B" ^
          "\xAC\xF0\xF8\x42\x35\xB3\x00\x54" ^
          "\x6A\xF8\xB9\x36\xF0\x35\x18\x5E" ^
          "\x2A\x8E\x9E\x0A\x67\xC8\x92\x4F"
        )
      )
  }
end


module Node = struct
  type t = {
    addr : Node_addr.t;
    ckey : Box.channel_key;
  }


  let create sk addr = {
    addr;
    ckey = Box.precompute sk addr.Node_addr.key;
  }
end


type t = {
  sk : Box.secret_key;
  pk : Box.public_key;
  nodes : Node.t PublicKeyMap.t;
}


module EncryptedPacket = struct

  let public_key =
    PacketType.Data (
      (fun ctx packet public_key ->
         let bytes = Box.Bytes.of_public_key public_key in
         Output.add_bytes packet bytes Box.public_key_size;
         ctx),
      (fun ((dht, public_key, nonce) as ctx) packet ->
         let packet, public_key' = Input.read_bytes packet Box.public_key_size in
         let public_key' = Box.Bytes.to_public_key public_key' in
         let ctx =
           match public_key with
           | None ->
               (dht, Some public_key', nonce)
           | Some _ ->
               ctx
         in
         ctx, packet, public_key')
    )


  let nonce =
    PacketType.Data (
      (fun ctx packet nonce ->
         let bytes = Box.Bytes.of_nonce nonce in
         Output.add_bytes packet bytes Box.nonce_size;
         ctx),
      (fun (dht, public_key, nonce) packet ->
         (*print_endline "reading nonce";*)
         assert (nonce = None);
         let packet, nonce = Input.read_bytes packet Box.nonce_size in
         let nonce = Box.Bytes.to_nonce nonce in
         (dht, public_key, Some nonce), packet, nonce)
    )


  let encrypted fmt =
    PacketType.Data (
      (fun ctx packet value ->
         let plain = Output.create 128 in
         let ctx = Packet.encode ctx plain fmt value in
         let (channel_key, nonce) = ctx in
         let cipher = Box.Bytes.fast_box channel_key (Output.to_bytes plain) nonce in
         Output.add_bytes packet cipher (Bytes.length cipher);
         ctx),
      (fun ctx packet ->
         (*print_endline "reading encrypted";*)
         let packet, data = Input.read packet in
         let (dht, public_key, nonce) = ctx in
         let node = PublicKeyMap.find_exn dht.nodes (Option.value_exn
                                                       public_key) in
         let channel_key = node.Node.ckey in
         let nonce = Option.value_exn nonce in
         let plain = Box.Bytes.fast_box_open channel_key data nonce in
         (*print_endline "decode encrypted";*)
         Packet.decode ctx (Input.of_bytes plain) fmt)
    )

end


module DhtPacket = struct

  open Packet
  open PacketType
  open EncryptedPacket


  module Kind = struct
    let echo_request     rest = Constant.uint8 0x00 rest
    let echo_response    rest = Constant.uint8 0x01 rest

    let nodes_request    rest = Constant.uint8 0x02 rest
    let nodes_response   rest = Constant.uint8 0x04 rest

    let cookie_request   rest = Constant.uint8 0x18 rest
    let cookie_response  rest = Constant.uint8 0x19 rest

    let crypto_handshake rest = Constant.uint8 0x1a rest
    let crypto_data      rest = Constant.uint8 0x1b rest
    let crypto           rest = Constant.uint8 0x20 rest

    let lan_discovery    rest = Constant.uint8 0x21 rest
  end


  let dht_packet kind contents =
    kind (
      public_key @::
      nonce @::
      encrypted contents
    )


  let echo_request =
    dht_packet Kind.echo_request (
      Kind.echo_request (
        uint64
      )
    )

  let echo_response =
    dht_packet Kind.echo_response (
      Kind.echo_response (
        uint64
      )
    )


  let nodes_request =
    dht_packet Kind.nodes_request (
      public_key @::
      uint64
    )


  type protocol =
    | UDP
    | TCP


  let protocol =
    Data (
      (fun ctx packet value ->
         Output.add_uint1 packet (
           match value with
           | UDP -> 0
           | TCP -> 1
         );
         ctx),
      (fun ctx packet ->
         let packet, bit = Input.read_uint1 packet in
         ctx, packet, match bit with
         | 0 -> UDP
         | 1 -> TCP
         | _ -> assert false)
    )


  let ipv4_format =
    protocol @::
    Constant.uint7 0b10 (
      bytes 4
    )

  let ipv6_format =
    protocol @::
    Constant.uint7 0b1010 (
      bytes 16
    )


  let node_format =
    Choice (ipv4_format, ipv6_format) @::
    uint16 @::
    public_key


  let nodes_response =
    dht_packet Kind.nodes_response (
      Repeated (uint8, node_format) @::
      uint64
    )

end


let create () =
  let sk, pk = Box.random_keypair () in
  let nodes = PublicKeyMap.empty in
  { sk; pk; nodes; }


let bootstrap dht addr =
  { dht with
    nodes =
      PublicKeyMap.add dht.nodes
        ~key:addr.Node_addr.key
        ~data:(Node.create dht.sk addr)
  }


let pack_nodes_request dht pk =
  Option.map (PublicKeyMap.find dht.nodes pk)
    ~f:(
      fun node ->
        let nonce = Box.random_nonce () in
        let ping_id = 0x8765432101234567L in

        let out = Output.create 1500 in
        ignore (
          Packet.encode
            (node.Node.ckey, nonce) out DhtPacket.nodes_request
            PacketType.(
              dht.pk @<<
              nonce @<<
              pk @<<
              ping_id
            )
        );
        Output.to_bytes out |> Bytes.to_string
    )


let unpack_nodes_response dht packet =
  let ctx, packet, decoded =
    Packet.decode (dht, None, None) packet DhtPacket.nodes_response
  in

  decoded
