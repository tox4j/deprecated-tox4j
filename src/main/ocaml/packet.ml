open Core.Std

exception Format_error of string


module Kind = struct
  type t =
    | EchoRequest
    | EchoResponse
    | NodesRequest
    | NodesResponse


  let to_string = function
    | EchoRequest   -> "EchoRequest"
    | EchoResponse  -> "EchoResponse"
    | NodesRequest  -> "NodesRequest"
    | NodesResponse -> "NodesResponse"


  let unpack ~buf =
    let open Or_error in
    match Message.Consume.uint8 buf with
    | 0x00 -> return EchoRequest
    | 0x01 -> return EchoResponse
    | 0x02 -> return NodesRequest
    | 0x04 -> return NodesResponse
    | _ -> of_exn (Format_error "PacketKind")


  let to_int = function
    | EchoRequest   -> 0x00
    | EchoResponse  -> 0x01
    | NodesRequest  -> 0x02
    | NodesResponse -> 0x04


  let pack ~buf kind =
    Message.Fill.uint8 buf (to_int kind)
end


module Data = struct
  type echo_request = {
    ping_id : int64;
  }

  type echo_response = {
    ping_id : int64;
  }

  type nodes_response = {
    nodes   : Types.node list;
    ping_id : int64;
  }

  type nodes_request = {
    key : PublicKey.t;
    ping_id : int64;
  }


  type t =
    | EchoRequest   of echo_request
    | EchoResponse  of echo_response
    | NodesRequest  of nodes_request
    | NodesResponse of nodes_response


  let kind = function
    | EchoRequest   _ -> Kind.EchoRequest
    | EchoResponse  _ -> Kind.EchoResponse
    | NodesRequest  _ -> Kind.NodesRequest
    | NodesResponse _ -> Kind.NodesResponse
end


module type S = sig
  type t

  val unpack :
    dht:Dht.t
    -> buf:(Message.cipher, read_only, Iobuf.seek) Message.t
    -> (Types.connected_node * t) Or_error.t

  val wrap : t -> Data.t
end


let unpack_repeated ~buf ~size ~f =
  let open Or_error in
  let rec loop acc = function
    | 0 ->
        return (List.rev acc)
    | n ->
        f ~buf >>= fun v ->
        loop (v :: acc) (n - 1)
  in

  let len = size buf in
  loop [] len


let pack_repeated ~buf ~size list ~f =
  size buf (List.length list);
  List.iter list ~f:(f ~buf)
