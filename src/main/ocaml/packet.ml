module Output = struct

  type t = {
    buffer : bytes;
    mutable bitpos : int;
  }


  let create length =
    {
      buffer = Bytes.make length '\x00';
      bitpos = 0;
    }


  let to_bytes out =
    assert (out.bitpos mod 8 = 0);
    Bytes.sub out.buffer 0 (out.bitpos / 8)


  let add out rhs =
    if out.bitpos mod 8 == 0 && rhs.bitpos mod 8 == 0 then (
      Bytes.blit
        rhs.buffer
        0
        out.buffer
        (out.bitpos / 8)
        (rhs.bitpos / 8);
      out.bitpos <- out.bitpos + rhs.bitpos
    ) else (
      assert false
    )


  let add_uint1 out value =
    assert (value = 0 || value = 1);
    if out.bitpos mod 8 == 0 then (
      Bytes.set out.buffer (out.bitpos / 8) (Char.chr @@ value lsl 7);
      if value <> 0 then
        assert (Char.code (Bytes.get out.buffer (out.bitpos / 8)) = 0x80)
      else
        assert (Char.code (Bytes.get out.buffer (out.bitpos / 8)) = 0x00);
      out.bitpos <- out.bitpos + 1
    ) else (
      assert false
    )


  let add_uint7 out value =
    assert (value >= 0x00);
    assert (value <= 0x7f);
    if out.bitpos mod 8 == 1 then (
      let old = Char.code (Bytes.get out.buffer (out.bitpos / 8)) in
      assert (old land 0x7f = 0x00);
      Bytes.set out.buffer (out.bitpos / 8) (Char.chr @@ old land value);
      out.bitpos <- out.bitpos + 7
    ) else (
      assert false
    )

  let add_uint8 out value =
    assert (value >= 0);
    assert (value <= 0xff);
    if out.bitpos mod 8 == 0 then (
      Bytes.set out.buffer (out.bitpos / 8) (Char.chr @@ value);

      out.bitpos <- out.bitpos + 8
    ) else (
      assert false
    )

  let add_uint16 out value =
    if out.bitpos mod 8 == 0 then (
      add_uint8 out (value lsr 8 land 0xff);
      add_uint8 out (value       land 0xff);
    ) else (
      assert false
    )

  let add_uint32 out value =
    if out.bitpos mod 8 == 0 then (
      let hi = Int32.to_int @@ Int32.shift_right value 16 in
      let lo = (Int32.to_int @@ value) land 0xffff in

      add_uint16 out hi;
      add_uint16 out lo;
    ) else (
      assert false
    )

  let add_uint64 out value =
    if out.bitpos mod 8 == 0 then (
      let hi = Int64.to_int32 @@ Int64.shift_right value 32 in
      let lo = Int64.to_int32 @@ value in

      add_uint32 out hi;
      add_uint32 out lo;
    ) else (
      assert false
    )

  let add_bytes out data length =
    if out.bitpos mod 8 == 0 then (
      Bytes.blit data 0 out.buffer (out.bitpos / 8) length;
      out.bitpos <- out.bitpos + length * 8
    ) else (
      assert false
    )

  let add_string out data length =
    if out.bitpos mod 8 == 0 then (
      Bytes.blit data 0 out.buffer (out.bitpos / 8) length;
      out.bitpos <- out.bitpos + length * 8
    ) else (
      assert false
    )

end


module Input = struct

  type t = {
    data : bytes;
    bitpos : int;
  }


  let of_bytes data =
    { data; bitpos = 0 }


  let read self =
    (*print_endline "> read";*)
    if self.bitpos mod 8 == 0 then (
      let length = Bytes.length self.data - self.bitpos / 8 in
      let bytes = Bytes.create length in
      Bytes.blit self.data (self.bitpos / 8) bytes 0 length;
      { self with bitpos = self.bitpos + length * 8 }, bytes
    ) else (
      assert false
    )


  let read_uint1 self =
    (*print_endline "> read_uint1";*)
    if self.bitpos mod 8 == 0 then (
      let value = Char.code (Bytes.get self.data (self.bitpos / 8)) lsr 7 in
      assert (value = 0 || value = 1);
      { self with bitpos = self.bitpos + 1 }, value
    ) else (
      assert false
    )


  let read_uint7 self =
    (*print_endline "> read_uint7";*)
    if self.bitpos mod 8 == 1 then (
      let value = Char.code (Bytes.get self.data (self.bitpos / 8)) land 0x7f in
      { self with bitpos = self.bitpos + 7 }, value
    ) else (
      assert false
    )


  let read_uint8 self =
    if self.bitpos mod 8 == 0 then (
      let value = Char.code (Bytes.get self.data (self.bitpos / 8)) in
      (*Printf.printf "> read_uint8 0x%02x\n" value;*)
      { self with bitpos = self.bitpos + 8 }, value
    ) else (
      assert false
    )


  let read_uint16 self =
    if self.bitpos mod 8 == 0 then (
      let self, hi = read_uint8 self in
      let self, lo = read_uint8 self in

      let value = hi lsl 8 lor lo in

      self, value
    ) else (
      assert false
    )


  let read_uint32 self =
    if self.bitpos mod 8 == 0 then (
      let self, hi = read_uint16 self in
      let self, lo = read_uint16 self in

      let hi = Int32.shift_left (Int32.of_int hi) 16 in
      let lo = Int32.of_int lo in
      let value = Int32.logor hi lo in

      self, value
    ) else (
      assert false
    )


  let read_uint64 self =
    if self.bitpos mod 8 == 0 then (
      let self, hi = read_uint32 self in
      let self, lo = read_uint32 self in

      let hi = Int64.shift_left (Int64.of_int32 hi) 32 in
      let lo = Int64.of_int32 lo in
      let value = Int64.logor hi lo in

      self, value
    ) else (
      assert false
    )


  let read_string self length =
    if self.bitpos mod 8 == 0 then (
      let bytes = Bytes.create length in
      Bytes.blit self.data (self.bitpos / 8) bytes 0 length;
      { self with bitpos = self.bitpos + length * 8 }, bytes
    ) else (
      assert false
    )

end


type ('a, 'b) choice =
  | A of 'a
  | B of 'b

type (_, _) packet =
  (* Constants *)
  | Constant
    :  ('c -> Output.t -> unit) * ('c -> Input.t -> 'c * Input.t) * ('c, 'b) packet
    -> ('c, 'b) packet

  (* Data *)
  | Data
    : ('c -> Output.t -> 'a -> unit) * ('c -> Input.t -> 'c * Input.t * 'a)
    -> ('c, 'a) packet

  (* List of packet formats *)
  | Cons
    : ('c, 'a) packet * ('c, 'b) packet
    -> ('c, 'a * 'b) packet

  (* Other operators *)
  | Repeated
    : ('c, int) packet * ('c, 'b) packet
    -> ('c, 'b list) packet

  | Choice
    : ('c, 'a) packet * ('c, 'b) packet
    -> ('c, ('a, 'b) choice) packet


let ( @:: ) a b = Cons (a, b)
let ( @<< ) a b = (a, b)


let rec decode : type a. 'c -> Input.t -> ('c, a) packet -> 'c * Input.t * a =
fun ctx packet fmt ->
  match fmt with
  | Constant (encoder, decoder, fmt) ->
      (*print_endline "Constant";*)
      let ctx, packet = decoder ctx packet in
      decode ctx packet fmt

  | Data (encoder, decoder) ->
      (*print_endline "Data";*)
      decoder ctx packet

  | Cons (a, b) ->
      let ctx, packet, a = decode ctx packet a in
      let ctx, packet, b = decode ctx packet b in
      ctx, packet, (a, b)

  | Repeated (length_fmt, fmt) ->
      (*print_endline "Repeated";*)
      let rec loop packet acc = function
        | 0 -> (ctx, packet, acc)
        | n ->
            let ctx, packet, value = decode ctx packet fmt in
            loop packet (value :: acc) (n - 1)
      in

      let ctx, packet, length = decode ctx packet length_fmt in
      loop packet [] length

  | Choice (a, b) ->
      (*print_endline "Choice";*)
      try
        let ctx, packet, value = decode ctx packet a in
        ctx, packet, A value
      with _ ->
        let ctx, packet, value = decode ctx packet b in
        ctx, packet, B value


let rec encode : type a. 'c -> Output.t -> ('c, a) packet -> a -> unit =
fun ctx packet fmt value ->
  match fmt with
  | Constant (encoder, decoder, fmt) ->
      encoder ctx packet;
      encode ctx packet fmt value

  | Data (encoder, decoder) ->
      encoder ctx packet value

  | Cons (a, b) ->
      encode ctx packet a (fst value);
      encode ctx packet b (snd value);

  | Repeated (length_fmt, fmt) ->
      encode ctx packet length_fmt (List.length value);
      List.iter
        (fun elt ->
           encode ctx packet fmt elt)
        value

  | Choice (a, b) ->
      match value with
      | A value -> encode ctx packet a value
      | B value -> encode ctx packet b value


module Constant = struct
  let uint7 value rest =
    Constant (
      (fun ctx packet -> Output.add_uint7 packet value),
      (fun ctx packet ->
         let packet, read = Input.read_uint7 packet in
         assert (read = value);
         ctx, packet),
      rest
    )

  let uint8 value rest =
    Constant (
      (fun ctx packet -> Output.add_uint8 packet value),
      (fun ctx packet ->
         let packet, read = Input.read_uint8 packet in
         if read <> value then
           failwith (
             Printf.sprintf "expected 0x%02x, but got 0x%02x\n"
               value read
           );
         ctx, packet),
      rest
    )
end


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


let (+:) a (b, c) = (a, b, c)


let uint1 : ('c, int) packet = 
  Data (
    (fun ctx packet value ->
       Output.add_uint1 packet value),
    (fun ctx packet ->
       ctx +: Input.read_uint1 packet)
  )


let uint8 : ('c, int) packet = 
  Data (
    (fun ctx packet value ->
       Output.add_uint8 packet value),
    (fun ctx packet ->
       ctx +: Input.read_uint8 packet)
  )


let uint16 : ('c, int) packet = 
  Data (
    (fun ctx packet value ->
       Output.add_uint16 packet value),
    (fun ctx packet ->
       ctx +: Input.read_uint16 packet)
  )


let uint32 : ('c, int32) packet = 
  Data (
    (fun ctx packet value ->
       Output.add_uint32 packet value),
    (fun ctx packet ->
       ctx +: Input.read_uint32 packet)
  )


let uint64 : ('c, int64) packet = 
  Data (
    (fun ctx packet value ->
       Output.add_uint64 packet value),
    (fun ctx packet ->
       ctx +: Input.read_uint64 packet)
  )


let bytes length =
  Data (
    (fun ctx packet data ->
       Output.add_string packet data length),
    (fun ctx packet ->
       (*print_endline "reading bytes";*)
       ctx +: Input.read_string packet length)
  )


open Sodium


let public_key : ('c, Box.public_key) packet = 
  Data (
    (fun ctx packet public_key ->
       let bytes = Box.Bytes.of_public_key public_key in
       Output.add_bytes packet bytes Box.public_key_size),
    (fun ctx packet ->
       (*print_endline "reading public_key";*)
       let packet, key = Input.read_string packet Box.public_key_size in
       ctx, packet, Box.Bytes.to_public_key key)
  )

let nonce : ('c, Box.nonce) packet = 
  Data (
    (fun ctx packet nonce ->
       let bytes = Box.Bytes.of_nonce nonce in
       Output.add_bytes packet bytes Box.nonce_size),
    (fun (channel_key, nonce) packet ->
       (*print_endline "reading nonce";*)
       let packet, nonce = Input.read_string packet Box.nonce_size in
       let nonce = Box.Bytes.to_nonce nonce in
       (channel_key, nonce), packet, nonce)
  )

let encrypted : ('c, 'a) packet -> ('c, 'a) packet = fun fmt ->
  Data (
    (fun ctx packet value ->
       let plain = Output.create 128 in
       encode ctx plain fmt value;
       let (channel_key, nonce) = ctx in
       let cipher = Box.Bytes.fast_box channel_key (Output.to_bytes plain) nonce in
       Output.add_bytes packet cipher (Bytes.length cipher)),
    (fun ctx packet ->
       (*print_endline "reading encrypted";*)
       let packet, data = Input.read packet in
       let (channel_key, nonce) = ctx in
       let plain = Box.Bytes.fast_box_open channel_key data nonce in
       (*print_endline "decode encrypted";*)
       decode ctx (Input.of_bytes plain) fmt)
  )


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
       )),
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



let main () =
  let nonce = Box.random_nonce () in
  let (sk, pk) = Box.random_keypair () in

  let ping_id = 0x8765432101234567L in

  let channel_key = Box.precompute sk pk in

  let out = Output.create 1500 in
  encode (channel_key, nonce) out echo_response (
    pk @<<
    nonce @<<
    ping_id
  );

  let ctx, packet, decoded =
    decode (channel_key, nonce)
      (Output.to_bytes out |> Input.of_bytes)
      echo_response
  in

  match decoded with
  | (pk', (nonce', ping_id')) ->
      assert (Box.equal_public_keys pk' pk);
      assert (nonce' = nonce);
      assert (ping_id' = ping_id);
;;


let (>>=) = Lwt.bind
let return = Lwt.return


let string_of_key key =
  let key = Box.Bytes.of_public_key key in
  let buf = Buffer.create 64 in
  Bytes.iter
    (fun b ->
       Printf.bprintf buf "%02x" (Char.code b))
    key;
  Buffer.contents buf


module StringMap = Map.Make(String)


let rec io_loop sock ip port key channel_keys =
  let target = Printf.sprintf "%s:%d" ip port in
  Printf.printf "Sending NodesRequest to %s %s\n" target (string_of_key key);

  let nonce = Box.random_nonce () in
  let (sk, pk) = Box.random_keypair () in

  let ping_id = 0x8765432101234567L in

  let channel_key = Box.precompute sk key in
  channel_keys := StringMap.add target (key, channel_key) !channel_keys;

  let out = Output.create 1500 in
  encode (channel_key, nonce) out nodes_request (
    pk @<<
    nonce @<<
    pk @<<
    ping_id
  );

  let packet = Output.to_bytes out in

  let addr = Unix.(ADDR_INET (inet_addr_of_string ip, port)) in

  Lwt_unix.sendto sock packet 0 (Bytes.length packet) [] addr >>= fun res ->
  (*Printf.printf "sendto: %d\n" res;*)

  let packet_data = Bytes.create 1500 in
  Lwt_unix.recvfrom sock packet_data 0 (Bytes.length packet_data) [] >>= fun (res, addr) ->
  let from =
    match addr with
    | Unix.ADDR_INET (addr, port) ->
        Unix.string_of_inet_addr addr ^ ":" ^ string_of_int port
    | Unix.ADDR_UNIX file ->
        file
  in
  Printf.printf "recvfrom: %d bytes from %s\n" res from;

  let packet = Input.of_bytes (Bytes.sub packet_data 0 res) in
  let (key, channel_key) = StringMap.find from !channel_keys in

  try
    match Bytes.get packet_data 0 with
    | '\x04' ->
        print_endline "Got NodesResponse";

        let ctx, packet, decoded =
          decode (channel_key, nonce) packet
            nodes_response
        in

        let next =
          match decoded with
          | pk', (nonce', (nodes, ping_id')) ->
              assert (Box.equal_public_keys pk' key);
              assert (ping_id' = ping_id);
              List.fold_left
                (fun acc -> function
                   | (A (proto, ipv4), (port, dht_key)) ->
                       assert (Bytes.length ipv4 = 4);
                       let ip =
                         Printf.sprintf "%d.%d.%d.%d"
                           (Char.code @@ Bytes.get ipv4 0)
                           (Char.code @@ Bytes.get ipv4 1)
                           (Char.code @@ Bytes.get ipv4 2)
                           (Char.code @@ Bytes.get ipv4 3)
                       in
                       print_endline ip;
                       (ip, port, dht_key) :: acc
                   | (B (proto, ipv6), (port, dht_key)) ->
                       assert (Bytes.length ipv6 = 16);
                       print_endline "yoy";
                       acc
                ) [] nodes
        in

        Lwt_list.iter_p
          (fun (ip, port, dht_key) ->
             io_loop sock ip port dht_key channel_keys
          )
          next

    | '\x00' ->
        print_endline "Got EchoRequest";
        let ctx, packet, decoded =
          decode (channel_key, nonce) packet
            echo_request
        in

        let nonce, ping_id = snd decoded in

        let out = Output.create 1500 in
        encode (channel_key, nonce) out echo_response (
          pk @<<
          nonce @<<
          ping_id
        );

        let packet = Output.to_bytes out in

        Lwt_unix.sendto sock packet 0 (Bytes.length packet) [] addr >>= fun res ->
        print_endline "Sent EchoResponse";
        return ()

    | x ->
        Printf.printf "Unhandled packet: %02x\n"
          (Char.code x);
        return ()

  with Sodium.Verification_failure ->
    print_endline @@ "Sodium.Verification_failure for packet from " ^ ip;
    return ()


let () =
  let sock = Lwt_unix.(socket PF_INET SOCK_DGRAM 0) in
  Lwt_unix.(bind sock (ADDR_INET (Unix.inet_addr_any, 33445)));

  let ip = "144.76.60.215" in
  let port = 33445 in
  let key =
    Box.Bytes.to_public_key (
      Bytes.of_string
        "\x04\x11\x9E\x83\x5D\xF3\xE7\x8B\xAC\xF0\xF8\x42\x35\xB3\x00\x54\x6A\xF8\xB9\x36\xF0\x35\x18\x5E\x2A\x8E\x9E\x0A\x67\xC8\x92\x4F"
    )
  in

  Lwt_main.run (io_loop sock ip port key (ref StringMap.empty))
