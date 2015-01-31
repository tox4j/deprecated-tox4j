open Core.Std
open Async.Std
open Types


type control_event =
  | Bootstrap of node


let recv_loop ~dht_ref ~sock ~recv_write =
  Udp.recvfrom_loop (Socket.fd sock)
    (fun buf from ->
       let buf = Message.of_iobuf buf in
       match NetworkPacket.unpack ~dht:!dht_ref ~buf with
       | Result.Ok handled ->
           Pipe.write_without_pushback recv_write handled
       | Result.Error exn ->
           print_endline (
             "From " ^ Socket.Address.Inet.to_string from ^
             ": error handling packet: " ^ Error.to_string_hum exn
           )
    )


let send_loop ~dht_ref ~sock ~send_read =
  let sendto =
    match Udp.sendto () with
    | Error error ->
        Error.raise error
    | Ok sendto ->
        sendto
  in

  let rec send_read_loop () =
    Pipe.read send_read >>= function
    | `Eof -> return ()
    | `Ok (node, packet) ->
        let buf = NetworkPacket.pack ~dht:!dht_ref ~node ~packet in

        let ip = InetAddr.to_string node.cn_node.n_addr in
        sendto (Socket.fd sock) (Message.to_iobuf buf)
          (Socket.Address.Inet.create
             (Unix.Inet_addr.of_string ip)
             ~port:(Port.to_int node.cn_node.n_port))

        >>= send_read_loop
  in

  send_read_loop ()


let network_loop ~dht_ref ~recv_write ~send_read =
  Udp.bind (Socket.Address.Inet.create Unix.Inet_addr.bind_any ~port:23445)
  >>= fun sock ->

  Deferred.all_ignore [
    recv_loop ~dht_ref ~sock ~recv_write;
    send_loop ~dht_ref ~sock ~send_read;
  ]


let event_loop ~dht_ref ~recv_read ~send_write ~handle_network_event =
  let rec recv_read_loop () =
    Pipe.read recv_read >>= function
    | `Eof ->
        return ()
    | `Ok decoded ->
        handle_network_event ~dht_ref ~send_write decoded
        >>= recv_read_loop
  in

  recv_read_loop ()


let control_loop ~dht_ref ~control_read ~send_write =
  let rec control_read_loop () =
    Pipe.read control_read >>= function
    | `Eof ->
        return ()
    | `Ok control ->
        begin match control with
          | Bootstrap node ->
              let dht = Dht.add_node ~dht:!dht_ref node in
              dht_ref := dht;

              let connected_node =
                Or_error.ok_exn (
                  Dht.node_by_key ~dht node.n_key
                )
              in

              let request =
                Packet.Data.(
                  NodesRequest {
                    key = dht.dht_pk;
                    ping_id = 0x8765432101234567L;
                  }
                )
              in

              Pipe.write send_write (connected_node, request)
        end
        >>= control_read_loop
  in

  control_read_loop ()


let create ~dht ~control_read ~handle_network_event =
  let recv_read, recv_write = Pipe.create () in
  let send_read, send_write = Pipe.create () in

  let dht_ref = ref dht in

  Deferred.all_ignore [
    control_loop ~dht_ref ~control_read ~send_write;
    event_loop   ~dht_ref ~recv_read ~send_write ~handle_network_event;
    network_loop ~dht_ref ~recv_write ~send_read;
  ]
