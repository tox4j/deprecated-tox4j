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
    NodesRequest (PublicKey const &sender, Nonce const &nonce,
                  CryptoBox const &box,
                  PublicKey const &client_id, uint64_t ping_id);
  };
}
