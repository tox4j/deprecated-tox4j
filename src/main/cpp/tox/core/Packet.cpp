#include "Packet.h"
#include "lwt/logging.h"

using namespace tox;


template<typename MessageFormat>
MessageFormat &
tox::operator << (MessageFormat &packet, PacketKind kind)
{
  return packet << static_cast<byte> (kind);
}


template<typename MessageFormat>
BitStream<MessageFormat>
tox::operator >> (BitStream<MessageFormat> const &packet, PacketKind &kind)
{
  uint8_t value;
  auto rest = packet >> value;
  kind = static_cast<PacketKind> (value);
  return rest;
}


template CipherText &tox::operator << <CipherText> (CipherText &packet, PacketKind kind);
template PlainText  &tox::operator << <PlainText > (PlainText  &packet, PacketKind kind);
template BitStream<CipherText> tox::operator >> <CipherText> (BitStream<CipherText> const &packet, PacketKind &kind);
template BitStream<PlainText > tox::operator >> <PlainText > (BitStream<PlainText>  const &packet, PacketKind &kind);
