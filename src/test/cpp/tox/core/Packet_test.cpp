#include "tox/core/Packet.h"
#include "tox/core/Logging.h"
#include <gtest/gtest.h>

using namespace tox;


TEST (Packet, Bitfield) {
  using Format = PacketFormat<
    PacketKind::PingResponse,
    encrypted<
      bitfield::type<
        bitfield::member<uint8_t, 1>,
        bitfield::member<std::integral_constant<uint8_t, __extension__ 0b0000010>, 7>
      >
    >
  >;

  KeyPair key_pair;
  CryptoBox box (key_pair);
  Nonce nonce;

  Packet<Format> packet (box, nonce, 1);

  output_hex (std::cout, packet.data (), packet.size ());
  std::cout << "\n";
}
