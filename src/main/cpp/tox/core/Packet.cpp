#include "Packet.h"
#include "Logging.h"

using namespace tox;


CipherText &
tox::operator << (CipherText &packet, PacketKind kind)
{
  return packet << static_cast<byte> (kind);
}


BitStream<CipherText>
tox::operator >> (BitStream<CipherText> const &packet, PacketKind &kind)
{
  uint8_t value;
  auto rest = packet >> value;
  kind = static_cast<PacketKind> (value);
  return rest;
}
