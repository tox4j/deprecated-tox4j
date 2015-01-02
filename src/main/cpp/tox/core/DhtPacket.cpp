#include "DhtPacket.h"

using namespace tox;


DhtPacket::DhtPacket (PacketKind kind, PublicKey const &sender, Nonce const &nonce,
                      CryptoBox const &box,
                      PlainText const &plain)
  : Packet (kind)
{
  *this << sender;
  *this << nonce;
  *this << box.encrypt (plain, nonce);
}
