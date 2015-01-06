#include "tox/core/Packet.h"
#include "tox/core/Logging.h"
#include <gtest/gtest.h>

#include "tox/core/KeyPair.h"
#include "tox/core/Nonce.h"

using namespace tox;


TEST (Packet, Plain) {
  using Format = PacketFormat<
    PacketKind::PingResponse,
    uint8_t,
    uint8_t
  >;

  Packet<Format> packet (0x99, 0xaa);
  std::cout << "Packet data: ";
  output_hex (std::cout, packet.data (), packet.size ());
  std::cout << "\n";

  Partial<int> result = packet.decode () ->* [&](uint8_t &&first, uint8_t second) {
    EXPECT_EQ (0x99, first);
    EXPECT_EQ (0xaa, second);
    return success (1234);
  };

  EXPECT_TRUE (result.ok ());
  result ->* [](int i) { EXPECT_EQ (1234, i); return success (); };
}


TEST (Packet, PlainNonceLast) {
  using Format = PacketFormat<
    PacketKind::PingResponse,
    uint8_t,
    Nonce
  >;

  Nonce orig_nonce = Nonce::random ();

  Packet<Format> packet (0x99, orig_nonce);
  std::cout << "Packet data: ";
  output_hex (std::cout, packet.data (), packet.size ());
  std::cout << "\n";

  Partial<int> result = packet.decode () ->* [&](uint8_t first, Nonce nonce) {
    EXPECT_EQ (orig_nonce, nonce);
    EXPECT_EQ (0x99, first);
    return success (1234);
  };

  EXPECT_TRUE (result.ok ());
  result ->* [](int i) { EXPECT_EQ (1234, i); return success (); };
}


TEST (Packet, PlainNonceFirst) {
  using Format = PacketFormat<
    PacketKind::PingResponse,
    Nonce,
    uint8_t
  >;

  Nonce orig_nonce = Nonce::random ();

  Packet<Format> packet (orig_nonce, 0x99);
  std::cout << "Packet data: ";
  output_hex (std::cout, packet.data (), packet.size ());
  std::cout << "\n";

  Partial<int> result = packet.decode () ->* [&](Nonce nonce, uint8_t second) {
    EXPECT_EQ (orig_nonce, nonce);
    EXPECT_EQ (0x99, second);
    return success (1234);
  };

  EXPECT_TRUE (result.ok ());
  result ->* [](int i) { EXPECT_EQ (1234, i); return success (); };
}


TEST (Packet, Simple) {
  using Format = PacketFormat<
    PacketKind::PingResponse,
    uint8_t,
    Nonce,
    encrypted<
      uint8_t
    >
  >;

  KeyPair key_pair;
  CryptoBox box (key_pair);
  Nonce orig_nonce = Nonce::random ();

  Packet<Format> packet (0x99, orig_nonce, box, 0xaa);
  std::cout << "Packet data: ";
  output_hex (std::cout, packet.data (), packet.size ());
  std::cout << "\n";

  Partial<int> result = packet.decode (box) ->* [&](uint8_t &&first, Nonce nonce, uint8_t second) {
    EXPECT_EQ (orig_nonce, nonce);
    EXPECT_EQ (0x99, first);
    EXPECT_EQ (0xaa, second);
    return success (1234);
  };

  EXPECT_TRUE (result.ok ());
  result ->* [](int i) { EXPECT_EQ (1234, i); return success (); };
}


TEST (Packet, NoncePacket) {
  using Format = PacketFormat<
    PacketKind::PingResponse,
    uint8_t,
    Nonce,
    encrypted<
      Nonce,
      uint8_t
    >
  >;

  KeyPair key_pair;
  CryptoBox box (key_pair);
  Nonce orig_nonce = Nonce::random ();

  Packet<Format> packet (0x99, orig_nonce, box, orig_nonce, 0xaa);
  std::cout << "Packet data: ";
  output_hex (std::cout, packet.data (), packet.size ());
  std::cout << "\n";

  Partial<int> result = packet.decode (box) ->* [&](uint8_t &&first, Nonce nonce, Nonce &&encrypted_nonce, uint8_t second) {
    EXPECT_EQ (orig_nonce, nonce);
    EXPECT_EQ (orig_nonce, encrypted_nonce);
    EXPECT_EQ (0x99, first);
    EXPECT_EQ (0xaa, second);
    return success (1234);
  };

  EXPECT_TRUE (result.ok ());
  result ->* [](int i) { EXPECT_EQ (1234, i); return success (); };
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
  Nonce orig_nonce = Nonce::random ();

  Packet<Format> packet (orig_nonce, 0x99, box, 1);
  std::cout << "Packet data: ";
  output_hex (std::cout, packet.data (), packet.size ());
  std::cout << "\n";

  auto result = packet.decode (box) ->* [&](Nonce nonce, uint8_t &&first, uint8_t second) {
    EXPECT_EQ (orig_nonce, nonce);
    EXPECT_EQ (0x99, first);
    EXPECT_EQ (1, second);
    return success ();
  };

  EXPECT_TRUE (result.ok ());
}


#if 0
TEST (Packet, Repeated) {
  using Format = PacketFormat<
    PacketKind::PingResponse,
    Nonce,
    encrypted<
      repeated<uint8_t, uint8_t>
    >
  >;

  KeyPair key_pair;
  CryptoBox box (key_pair);
  Nonce orig_nonce = Nonce::random ();

  std::vector<uint8_t> data = { 1, 2, 3, 4 };

  Packet<Format> packet (orig_nonce, box, data);
  std::cout << "Packet data: ";
  output_hex (std::cout, packet.data (), packet.size ());
  std::cout << "\n";

  auto result = packet.decode (box) ->* [&](Nonce nonce, std::vector<uint8_t> &&first) {
    EXPECT_EQ (orig_nonce, nonce);
    EXPECT_EQ (data, first);
    return success ();
  };

  EXPECT_TRUE (result.ok ());
}
#endif


TEST (Packet, RepeatedTuple) {
  using Format = PacketFormat<
    PacketKind::PingResponse,
    Nonce,
    encrypted<
      repeated<uint8_t, uint8_t, uint8_t>
    >
  >;

  KeyPair key_pair;
  CryptoBox box (key_pair);
  Nonce orig_nonce = Nonce::random ();

  std::vector<std::tuple<uint8_t, uint8_t>> data = {
    std::tuple<uint8_t, uint8_t> { 1, 2 },
    std::tuple<uint8_t, uint8_t> { 3, 4 },
  };
  Packet<Format> packet (orig_nonce, box, data);
  std::cout << "Packet data: ";
  output_hex (std::cout, packet.data (), packet.size ());
  std::cout << "\n";

  auto result = packet.decode (box) ->* [&](Nonce nonce, std::vector<std::tuple<uint8_t, uint8_t>> second) {
    EXPECT_EQ (orig_nonce, nonce);
    EXPECT_EQ (data, second);
    return success ();
  };

  EXPECT_TRUE (result.ok ());
}


TEST (Packet, Choice) {
  using Format = PacketFormat<
    PacketKind::PingResponse,
    Nonce,
    encrypted<
      choice<
        PacketFormatTag<
          uint8_t,
          std::integral_constant<uint8_t, 1>,
          uint8_t
        >,
        PacketFormatTag<
          uint8_t,
          std::integral_constant<uint8_t, 2>,
          uint8_t,
          uint8_t
        >,
        PacketFormatTag<
          uint8_t,
          std::integral_constant<uint8_t, 3>,
          uint8_t,
          uint8_t,
          uint8_t
        >
      >
    >
  >;

  KeyPair key_pair;
  CryptoBox box (key_pair);
  Nonce orig_nonce = Nonce::random ();

  typedef variant<
    std::tuple<uint8_t, uint8_t>,
    std::tuple<uint8_t, uint8_t, uint8_t>,
    std::tuple<uint8_t, uint8_t, uint8_t, uint8_t>
  > data_type;
  static_assert (sizeof (data_type) == 5, "");
  variant_type<1, data_type>::type data { 1, 2, 3 };
  Packet<Format> packet (orig_nonce, box, data_type (data));
  std::cout << "Packet data: ";
  output_hex (std::cout, packet.data (), packet.size ());
  std::cout << "\n";

  auto result = packet.decode (box) ->* [&](Nonce nonce, data_type second) {
    EXPECT_EQ (orig_nonce, nonce);
    second.visit<void> () >>= {
      [](std::tuple<uint8_t, uint8_t> const &) {
        EXPECT_TRUE (false);
      },

      [](std::tuple<uint8_t, uint8_t, uint8_t> const &a) {
        EXPECT_EQ (1, std::get<0> (a));
        EXPECT_EQ (2, std::get<1> (a));
        EXPECT_EQ (3, std::get<2> (a));
      },

      [](std::tuple<uint8_t, uint8_t, uint8_t, uint8_t> const &) {
        EXPECT_TRUE (false);
      },
    };
    return success ();
  };

  EXPECT_TRUE (result.ok ());
}


TEST (Packet, PlainChoice) {
  using Format = PacketFormat<
    PacketKind::PingResponse,
    choice<
      PacketFormatTag<
        uint8_t,
        std::integral_constant<uint8_t, 1>,
        uint8_t
      >,
      PacketFormatTag<
        uint8_t,
        std::integral_constant<uint8_t, 2>,
        uint8_t,
        uint8_t
      >,
      PacketFormatTag<
        uint8_t,
        std::integral_constant<uint8_t, 3>,
        uint8_t,
        uint8_t,
        uint8_t
      >
    >
  >;

  typedef variant<
    std::tuple<uint8_t, uint8_t>,
    std::tuple<uint8_t, uint8_t, uint8_t>,
    std::tuple<uint8_t, uint8_t, uint8_t, uint8_t>
  > data_type;
  static_assert (sizeof (data_type) == 5, "");
  variant_type<0, data_type>::type data { 4, 5 };
  Packet<Format> packet { data_type (data) };
  std::cout << "Packet data: ";
  output_hex (std::cout, packet.data (), packet.size ());
  std::cout << "\n";

  auto result = packet.decode () ->* [&](data_type second) {
    second.visit<void> () >>= {
      [](std::tuple<uint8_t, uint8_t> const &a) {
        EXPECT_EQ (4, std::get<0> (a));
        EXPECT_EQ (5, std::get<1> (a));
      },

      [](std::tuple<uint8_t, uint8_t, uint8_t> const &) {
        EXPECT_TRUE (false);
      },

      [](std::tuple<uint8_t, uint8_t, uint8_t, uint8_t> const &) {
        EXPECT_TRUE (false);
      },
    };
    return success ();
  };

  EXPECT_TRUE (result.ok ());
}
