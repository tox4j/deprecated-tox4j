open Sodium


let public_key =
  PacketType.Data (
    (fun ctx packet public_key ->
       let bytes = Box.Bytes.of_public_key public_key in
       Output.add_bytes packet bytes Box.public_key_size),
    (fun ctx packet ->
       (*print_endline "reading public_key";*)
       let packet, key = Input.read_string packet Box.public_key_size in
       ctx, packet, Box.Bytes.to_public_key key)
  )


let nonce =
  PacketType.Data (
    (fun ctx packet nonce ->
       let bytes = Box.Bytes.of_nonce nonce in
       Output.add_bytes packet bytes Box.nonce_size),
    (fun (channel_key, nonce) packet ->
       (*print_endline "reading nonce";*)
       let packet, nonce = Input.read_string packet Box.nonce_size in
       let nonce = Box.Bytes.to_nonce nonce in
       (channel_key, nonce), packet, nonce)
  )


let encrypted fmt =
  PacketType.Data (
    (fun ctx packet value ->
       let plain = Output.create 128 in
       Packet.encode ctx plain fmt value;
       let (channel_key, nonce) = ctx in
       let cipher = Box.Bytes.fast_box channel_key (Output.to_bytes plain) nonce in
       Output.add_bytes packet cipher (Bytes.length cipher)),
    (fun ctx packet ->
       (*print_endline "reading encrypted";*)
       let packet, data = Input.read packet in
       let (channel_key, nonce) = ctx in
       let plain = Box.Bytes.fast_box_open channel_key data nonce in
       (*print_endline "decode encrypted";*)
       Packet.decode ctx (Input.of_bytes plain) fmt)
  )
