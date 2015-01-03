#pragma once

#include "Message.h"

#include <sodium.h>


namespace tox
{
  struct IPv4Address
    : byte_array<4>
  {
    typedef byte_array<4> super;
  };

  struct IPv6Address
    : byte_array<16>
  {
    typedef byte_array<16> super;
  };


  PlainText &operator << (PlainText &packet, IPv4Address address);
  PlainText &operator << (PlainText &packet, IPv6Address address);
}
