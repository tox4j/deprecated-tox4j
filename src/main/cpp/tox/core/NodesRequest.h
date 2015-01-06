#pragma once

#include "DhtPacket.h"


namespace tox
{
  using NodesRequestFormat = DhtPacketFormat<
    PacketKind::NodesRequest,
    PublicKey,
    uint64_t
  >;

  struct NodesRequest
    : Packet<NodesRequestFormat>
  {
    using Packet<NodesRequestFormat>::Packet;
  };
}
