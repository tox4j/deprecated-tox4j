#pragma once

#include "DhtPacket.h"


namespace tox
{
  struct NodesRequest
    : private DhtPacket
  {
    NodesRequest (PublicKey const &sender, Nonce const &nonce,
                  CryptoBox const &box,
                  PublicKey const &client_id, uint64_t ping_id);
  };
}
