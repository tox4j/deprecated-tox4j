open PacketType


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
         if read <> value then
           failwith (
             Printf.sprintf "expected 0x%02x, but got 0x%02x\n"
               value read
           );
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
