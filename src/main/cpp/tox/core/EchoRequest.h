#pragma once

#include "DhtPacket.h"


namespace tox
{
  using EchoRequestFormat = DhtPacketFormat<
    PacketKind::EchoRequest,
    std::integral_constant<PacketKind, PacketKind::EchoRequest>,
    uint64_t
  >;

  struct EchoRequest
    : Packet<EchoRequestFormat>
  {
    using Packet<EchoRequestFormat>::Packet;
  };
}
