#include "NodesResponse.h"
#include "Logging.h"

using namespace tox;


PlainText &
tox::operator << (PlainText &packet, Protocol protocol)
{
  return packet << static_cast<byte> (protocol);
}


std::ostream &
tox::operator << (std::ostream &os, Protocol protocol)
{
  switch (protocol)
    {
    case Protocol::UDP:
      os << "UDP";
      break;
    case Protocol::TCP:
      os << "TCP";
      break;
    default:
      os << "<invalid protocol>";
      break;
    }
  return os;
}
