#pragma once

#include "types.h"


namespace tox
{
  enum class AddressFamily
  {
    IPv4,
    IPv6,
  };


  struct NetworkAddressData;

  struct NetworkAddress
    : PrivateType<NetworkAddressData>
  {
    static Partial<NetworkAddress> resolve (char const *node);

    NetworkAddress (NetworkAddress &&rhs);
    ~NetworkAddress ();

  private:
    NetworkAddress (pointer &&d);
  };
}
