#include "tox/core/Packet.h"
#include "tox/core/Logging.h"
#include <gtest/gtest.h>

#include "tox/core/KeyPair.h"
#include "tox/core/Nonce.h"

using namespace tox;


TEST (Packet, Simple) {
  using Format = PacketFormat<
    PacketKind::PingResponse,
    uint8_t,
    encrypted<
      uint8_t
    >
  >;

  KeyPair key_pair;
  CryptoBox box (key_pair);
  Nonce nonce { };

  Packet<Format> packet (0x99, box, nonce, 0xaa);

  CipherText t = CipherText::from_bytes (packet.data (), packet.size ());
  Partial<int> result = packet.decode (t, box, nonce) >>= [](uint8_t &&first, uint8_t second) {
    EXPECT_EQ (0x99, first);
    EXPECT_EQ (0xaa, second);
    return success (1234);
  };

  EXPECT_TRUE (result.ok ());
  result >>= [](int i) { EXPECT_EQ (1234, i); return success (); };

  output_hex (std::cout, packet.data (), packet.size ());
  std::cout << "\n";
}


TEST (Packet, NoncePacket) {
  using Format = PacketFormat<
    PacketKind::PingResponse,
    Nonce,
    uint8_t,
    encrypted<
      uint8_t
    >
  >;

  KeyPair key_pair;
  CryptoBox box (key_pair);
  Nonce nonce { };

  Packet<Format> packet (nonce, 0x99, box, nonce, 0xaa);

  CipherText t = CipherText::from_bytes (packet.data (), packet.size ());
  Partial<int> result = packet.decode (t, box, nonce) >>= [](Nonce nonce, uint8_t &&first, uint8_t second) {
    EXPECT_EQ (Nonce (), nonce);
    EXPECT_EQ (0x99, first);
    EXPECT_EQ (0xaa, second);
    return success (1234);
  };

  EXPECT_TRUE (result.ok ());
  result >>= [](int i) { EXPECT_EQ (1234, i); return success (); };

  output_hex (std::cout, packet.data (), packet.size ());
  std::cout << "\n";
}


TEST (Packet, Bitfield) {
  using Format = PacketFormat<
    PacketKind::PingResponse,
    Nonce,
    uint8_t,
    encrypted<
      bitfield::type<
        bitfield::member<uint8_t, 1>,
        bitfield::member<std::integral_constant<uint8_t, __extension__ 0b0000010>, 7>
      >
    >
  >;

  KeyPair key_pair;
  CryptoBox box (key_pair);
  Nonce nonce { };

  Packet<Format> packet (nonce, 0x99, box, nonce, 1);

  CipherText t = CipherText::from_bytes (packet.data (), packet.size ());
  auto result = packet.decode (t, box, nonce) >>= [](Nonce nonce, uint8_t &&first, uint8_t second) {
    EXPECT_EQ (Nonce (), nonce);
    EXPECT_EQ (0x99, first);
    EXPECT_EQ (1, second);
    return success ();
  };

  EXPECT_TRUE (result.ok ());

  output_hex (std::cout, packet.data (), packet.size ());
  std::cout << "\n";
}
