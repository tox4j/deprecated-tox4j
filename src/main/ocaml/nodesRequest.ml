open Core.Std
open Async.Std
open Types


type t = {
  key : PublicKey.t;
  ping_id : int64;
}


let pack ~public_key ~channel_key ~request packet =
  Iobuf.Fill.uint8 packet 0x02;

  let nonce = Nonce.random () in

  PublicKey.pack packet public_key;
  Nonce.pack packet nonce;

  Crypto.pack packet ~channel_key ~nonce ~f:(
    fun packet ->
      PublicKey.pack packet request.key;
      Iobuf.Fill.int64_t_be packet request.ping_id;
  )


let make ~public_key ~channel_key request =
  let packet = Iobuf.create Udp.default_capacity in
  pack ~public_key ~channel_key ~request packet;
  Iobuf.flip_lo packet;
  packet
