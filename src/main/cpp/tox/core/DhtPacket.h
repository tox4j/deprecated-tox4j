#pragma once

#include "CryptoBox.h"
#include "Packet.h"


namespace tox
{
  template<PacketKind Kind, typename ...Contents>
  using DhtPacketFormat = PacketFormat<
    Kind,
    PublicKey,
    Nonce,
    encrypted<Contents...>
  >;
}
