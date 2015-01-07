#pragma once

#include "DhtPacket.h"


namespace tox
{
  using EchoResponseFormat = DhtPacketFormat<
    PacketKind::EchoResponse,
    std::integral_constant<PacketKind, PacketKind::EchoResponse>,
    uint64_t
  >;

  struct EchoResponse
    : Packet<EchoResponseFormat>
  {
    using Packet<EchoResponseFormat>::Packet;
  };
}
