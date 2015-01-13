#include "IPAddress.h"
#include "lwt/logging.h"

using namespace tox;


std::ostream &
tox::operator << (std::ostream &os, IPv4Address const &address)
{
  os << (unsigned) address[0] << '.'
     << (unsigned) address[1] << '.'
     << (unsigned) address[2] << '.'
     << (unsigned) address[3];
  return os;
}
