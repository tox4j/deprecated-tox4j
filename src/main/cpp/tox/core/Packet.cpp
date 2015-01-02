#include "Packet.h"

using namespace tox;


Packet::Packet (PacketKind kind)
{
  byte_vector::push_back (static_cast<byte> (kind));
}


Packet &
Packet::operator << (byte b)
{
  byte_vector::push_back (b);
  return *this;
}


Packet &
Packet::operator << (CipherText const &encrypted)
{
  return append (encrypted);
}


Packet &
Packet::operator << (PublicKey const &key)
{
  return append (key);
}


Packet &
Packet::operator << (Nonce const &nonce)
{
  return append (nonce);
}


template<typename Iterable>
Packet &
Packet::append (Iterable const &data)
{
  byte_vector::insert (end (), data.cbegin (), data.cend ());
  return *this;
}
