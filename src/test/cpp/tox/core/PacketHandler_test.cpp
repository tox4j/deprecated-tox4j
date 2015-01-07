#include "tox/core/PacketHandler.h"
#include "tox/core/Logging.h"
#include <gtest/gtest.h>

#include "tox/core/KeyPair.h"
#include "tox/core/Nonce.h"

using namespace tox;


TEST (PacketHandler, Plain) {
  using Format = PacketFormat<
    PacketKind::EchoResponse,
    uint8_t,
    Nonce,
    encrypted<
      uint8_t
    >
  >;

  KeyPair key_pair;
  CryptoBox box (key_pair);
  Nonce orig_nonce = Nonce::random ();

  Packet<Format> const packet (0x99, orig_nonce, box, 0xaa);
  CipherText data = CipherText::from_bytes (packet.data (), packet.size ());

  struct Handler
    : PacketHandler<Handler, Format, int>
  {
    Nonce orig_nonce_;

    Handler (Nonce orig_nonce)
      : orig_nonce_ (orig_nonce)
    { }

    Partial<int> handle (uint8_t first, Nonce nonce, uint8_t second)
    {
      EXPECT_EQ (orig_nonce_, nonce);
      EXPECT_EQ (0x99, first);
      EXPECT_EQ (0xaa, second);
      return success (3);
    }
  };

  PacketDispatcher<int> dispatcher;
  dispatcher.register_handler<Handler> (orig_nonce);

  dispatcher.handle (std::move (data), box);
}
