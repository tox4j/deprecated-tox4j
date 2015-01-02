#pragma once

#include "CryptoBox.h"
#include "Packet.h"


namespace tox
{
  struct DhtPacket
    : private Packet
  {
    DhtPacket (PacketKind kind, PublicKey const &sender, Nonce const &nonce,
               CryptoBox const &box,
               PlainText const &plain);
  };
}
