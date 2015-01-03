#pragma once

#include "DhtPacket.h"
#include "IPAddress.h"


namespace tox
{
  enum class Protocol
    : byte
  {
    UDP = 0,
    TCP = 1,
  };

  using IPv4Format = PacketFormatTag<
    bitfield::type<
      bitfield::member<Protocol, 1>,
      bitfield::member<std::integral_constant<uint8_t, __extension__ 0b0000010>, 7>
    >,
    IPv4Address
  >;

  using IPv6Format = PacketFormatTag<
    bitfield::type<
      bitfield::member<Protocol, 1>,
      bitfield::member<std::integral_constant<uint8_t, __extension__ 0b0001010>, 7>
    >,
    IPv6Address
  >;

  using NodeFormat = PacketFormatTag<
    choice<IPv4Format, IPv6Format>,
    uint16_t,
    PublicKey
  >;

  using NodesResponseFormat = DhtPacketFormat<
    PacketKind::NodesResponse,
    repeated<uint8_t, NodeFormat>,
    uint64_t
  >;

  struct NodesResponse
    : Packet<NodesResponseFormat>
  {
#if 0
    NodesResponse ()
    {
    }
#endif
  };
}
