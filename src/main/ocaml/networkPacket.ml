open Core.Std

let unpack_and_wrap ~dht ~buf kind =
  let unpacker : (module Packet.S) =
    let open Packet.Kind in
    match kind with
    | EchoRequest   -> (module EchoRequest  )
    | EchoResponse  -> (module EchoResponse )
    | NodesRequest  -> (module NodesRequest )
    | NodesResponse -> (module NodesResponse)
  in

  let open Or_error in
  let module PacketModule = (val unpacker) in
  PacketModule.unpack ~dht ~buf >>| fun (node, decoded) ->
  node, PacketModule.wrap decoded


let unpack ~dht ~buf =
  let open Or_error in
  let buf = Iobuf.read_only buf in

  Packet.Kind.unpack ~buf >>= (unpack_and_wrap ~dht ~buf)


let pack ~dht ~node ~packet =
  let packer =
    let open Packet.Data in
    match packet with
    | EchoRequest   packet -> EchoRequest.pack   ~packet
    | EchoResponse  packet -> EchoResponse.pack  ~packet
    | NodesRequest  packet -> NodesRequest.pack  ~packet
    | NodesResponse packet -> NodesResponse.pack ~packet
  in

  let buf = Iobuf.create Async.Std.Udp.default_capacity in
  packer ~dht ~buf ~node;
  Iobuf.flip_lo buf;

  buf
