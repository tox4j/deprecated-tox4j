#pragma once

#include "DhtPacket.h"
#include "IPAddress.h"
#include "KeyPair.h"


namespace tox
{
  using IPv4Format = PacketFormatTag<
    bitfield::type<
      bitfield::member<Protocol, 1>,
      bitfield::member<std::integral_constant<uint8_t, 0b0000010>, 7>
    >,
    IPv4Address
  >;

  using IPv6Format = PacketFormatTag<
    bitfield::type<
      bitfield::member<Protocol, 1>,
      bitfield::member<std::integral_constant<uint8_t, 0b0001010>, 7>
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
    using Packet<NodesResponseFormat>::Packet;
  };


  PlainText &operator << (PlainText &packet, Protocol protocol);
  std::ostream &operator << (std::ostream &os, Protocol protocol);
}
