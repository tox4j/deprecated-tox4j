#include "Packet.h"
#include "Logging.h"

using namespace tox;


CipherText &
tox::operator << (CipherText &packet, PacketKind kind)
{
  return packet << static_cast<byte> (kind);
}
