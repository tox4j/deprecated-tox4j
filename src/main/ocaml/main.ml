open Core.Std
open Async.Std

open Types
open Sodium


let wanted_key dht =
  if true then
    dht.dht_pk
  else
    PublicKey.of_string (
      "\xF3\x2A\x31\x79\x72\xA6\x6F\x2C" ^
      "\x3B\x8B\x6F\x90\x9C\x90\x66\x31" ^
      "\xA8\x8A\xE0\xB4\x85\xA7\x2B\x45" ^
      "\x61\xA5\xD1\x11\x38\x71\x25\x04"
    )


let sonOfRa = {
  n_proto = Protocol.UDP;
  (*n_addr = "144.76.60.215";*)
  n_addr = InetAddr.IPv4 (Option.value_exn (InetAddr.ipv4_of_string
                                              "\x90\x4c\x3c\xd7"));
  n_port = Option.value_exn (Port.of_int 33445);
  n_key =
    PublicKey.of_string (
      "\x04\x11\x9E\x83\x5D\xF3\xE7\x8B" ^
      "\xAC\xF0\xF8\x42\x35\xB3\x00\x54" ^
      "\x6A\xF8\xB9\x36\xF0\x35\x18\x5E" ^
      "\x2A\x8E\x9E\x0A\x67\xC8\x92\x4F"
    )
}


let handle_network_event ~dht_ref ~send_write (from, packet) =
  let open Packet.Data in

  let node_count = PublicKeyMap.length !dht_ref.dht_nodes in
  print_string @@ "[" ^ string_of_int node_count ^ "] ";

  match packet with
  | EchoRequest { EchoRequest.ping_id } ->
      print_endline @@ "Got EchoRequest from " ^
                       InetAddr.to_string from.cn_node.n_addr ^ ":" ^
                       string_of_int (Port.to_int from.cn_node.n_port);
 
      (*
      print_endline @@ "Sending EchoResponse to " ^
                       InetAddr.to_string from.cn_node.n_addr ^ ":" ^
                       string_of_int (Port.to_int from.cn_node.n_port);
      *)
      (* Form an EchoResponse. *)
      let response = Packet.Data.(EchoResponse { ping_id }) in

      Pipe.write send_write (from, response)

  | EchoResponse _
  | NodesRequest _ ->
      return ()
  | NodesResponse response ->
      print_endline @@ "Got NodesResponse from " ^
                       InetAddr.to_string from.cn_node.n_addr ^ ":" ^
                       string_of_int (Port.to_int from.cn_node.n_port);

      let open NodesResponse in
      assert (response.ping_id = 0x8765432101234567L);

      List.map response.NodesResponse.nodes
        ~f:(
          fun node ->
            match node.n_addr with
            | InetAddr.IPv6 _ ->
                (*
                print_endline @@ "Ignoring IPv6 address " ^
                                 InetAddr.to_string node.n_addr ^ ":" ^
                                 string_of_int (Port.to_int node.n_port);
                *)
                return ()
            | InetAddr.IPv4 _ ->
                (*
                print_endline @@ "Sending NodesRequest to " ^
                                 InetAddr.to_string node.n_addr ^ ":" ^
                                 string_of_int (Port.to_int node.n_port);
                *)
                (* Create new channel key or update IP information. *)
                let dht = Dht.add_node !dht_ref node in

                (* Update DHT reference. *)
                dht_ref := dht;

                let node =
                  Or_error.ok_exn (Dht.node_by_key dht node.n_key)
                in

                print_endline @@ "dist(wanted_key) = " ^
                                 PublicKey.to_string_hum (
                                   PublicKey.distance
                                     (wanted_key dht) node.cn_node.n_key
                                   |> PublicKey.of_string
                                 );

                (* Form a NodesRequest. *)
                let request =
                  Packet.Data.(
                    NodesRequest {
                      key = wanted_key dht;
                      ping_id = 0x8765432101234567L;
                    }
                  )
                in

                Pipe.write send_write (node, request)
        )
      |> Deferred.all_ignore


let main =
  let dht = Dht.create () in

  let control_read, control_write = Pipe.create () in

  Deferred.all_ignore [
    Pipe.write control_write (Network.Bootstrap sonOfRa);
    Network.create ~dht ~control_read ~handle_network_event;
  ]


let () =
  (main >>> fun () -> shutdown 0);
  never_returns (Scheduler.go ())
