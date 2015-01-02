#pragma once

#include "Message.h"
#include "Nonce.h"
#include "KeyPair.h"


namespace tox
{
  enum class PacketKind
    : byte
  {
    PingRequest         = 0x00,
    PingResponse        = 0x01,

    NodesRequest        = 0x02,
    NodesResponse       = 0x04,

    CookieRequest       = 0x18,
    CookieResponse      = 0x19,

    CryptoHandshake     = 0x1a,
    CryptoData          = 0x1b,
    Crypto              = 0x20,

    LANDiscovery        = 0x21,
  };


  struct Packet
    : private byte_vector
  {
    Packet (PacketKind kind);

    Packet &operator << (byte b);
    Packet &operator << (CipherText const &encrypted);
    Packet &operator << (PublicKey const &key);
    Packet &operator << (Nonce const &nonce);

  private:
    template<typename Iterable>
    Packet &append (Iterable const &data);
  };
}
