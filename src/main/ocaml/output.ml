open Core.Std
open Binary_packing


type t = {
  buffer : string;
  mutable bitpos : int;
}


let create length =
  {
    buffer = String.make length '\x00';
    bitpos = 0;
  }


let to_string out =
  assert (out.bitpos mod 8 = 0);
  String.sub out.buffer 0 (out.bitpos / 8)


let to_bytes out =
  to_string out |> Bytes.unsafe_of_string


let add_uint1 out value =
  assert (value = 0 || value = 1);
  if out.bitpos mod 8 = 0 then (
    pack_unsigned_8 ~buf:out.buffer ~pos:(out.bitpos / 8) (value lsl 7);
    if value <> 0 then
      assert (Char.to_int out.buffer.[out.bitpos / 8] = 0x80)
    else
      assert (Char.to_int out.buffer.[out.bitpos / 8] = 0x00);
    out.bitpos <- out.bitpos + 1
  ) else (
    assert false
  )


let add_uint7 out value =
  assert (value >= 0x00);
  assert (value <= 0x7f);
  if out.bitpos mod 8 = 1 then (
    let old = unpack_unsigned_8 ~buf:out.buffer ~pos:(out.bitpos / 8) in
    assert (old land 0x7f = 0x00);
    pack_unsigned_8 ~buf:out.buffer ~pos:(out.bitpos / 8) (old lor value);
    out.bitpos <- out.bitpos + 7
  ) else (
    assert false
  )

let add_uint8 out value =
  assert (value >= 0);
  assert (value <= 0xff);
  if out.bitpos mod 8 = 0 then (
    pack_unsigned_8 ~buf:out.buffer ~pos:(out.bitpos / 8) value;
    out.bitpos <- out.bitpos + 8
  ) else (
    assert false
  )

let add_uint16 out value =
  if out.bitpos mod 8 = 0 then (
    pack_unsigned_16_big_endian ~buf:out.buffer ~pos:(out.bitpos / 8) value;
    out.bitpos <- out.bitpos + 16
  ) else (
    assert false
  )

let add_uint32 out value =
  if out.bitpos mod 8 = 0 then (
    pack_signed_32 ~byte_order:`Big_endian ~buf:out.buffer ~pos:(out.bitpos / 8) value;
    out.bitpos <- out.bitpos + 32
  ) else (
    assert false
  )

let add_uint64 out value =
  if out.bitpos mod 8 = 0 then (
    pack_signed_64 ~byte_order:`Big_endian ~buf:out.buffer ~pos:(out.bitpos / 8) value;
    out.bitpos <- out.bitpos + 64
  ) else (
    assert false
  )

let add_string out data length =
  if out.bitpos mod 8 = 0 then (
    pack_padded_fixed_string ~buf:out.buffer ~pos:(out.bitpos / 8) ~len:length data;
    out.bitpos <- out.bitpos + length * 8
  ) else (
    assert false
  )

let add_bytes out data length =
  add_string out (Bytes.unsafe_to_string data) length
