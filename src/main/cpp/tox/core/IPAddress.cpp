#include "IPAddress.h"
#include "Logging.h"

using namespace tox;


PlainText &
tox::operator << (PlainText &packet, IPv4Address address)
{
  //packet.write (address.data (), address.size ());
  return packet;
}


PlainText &
tox::operator << (PlainText &packet, IPv6Address address)
{
  //packet.write (address.data (), address.size ());
  return packet;
}
