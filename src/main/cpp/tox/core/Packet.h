#pragma once

#include "Packet/constructor.h"

#include <limits>


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


  CipherText &operator << (CipherText &packet, PacketKind kind);


  template<PacketKind Kind, typename ...Contents>
  using PacketFormat = PacketFormatTag<
    std::integral_constant<PacketKind, Kind>,
    Contents...
  >;


  template<typename Format>
  struct Packet
    : detail::packet_constructor_t<Format>
  {
    using detail::packet_constructor_t<Format>::packet_constructor_t;
  };
}
