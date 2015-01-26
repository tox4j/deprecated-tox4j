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
