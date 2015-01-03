#include "IPAddress.h"
#include "Logging.h"

using namespace tox;


PlainText &
tox::operator << (PlainText &packet, IPv4Address address)
{
  return packet;
}


PlainText &
tox::operator << (PlainText &packet, IPv6Address address)
{
  return packet;
}
