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
