open EncryptedPacket
open Packet
open PacketType


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
