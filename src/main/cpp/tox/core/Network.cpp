#include "Network.h"
#include "Logging.h"

#include <sys/types.h>
#include <sys/socket.h>
#include <netdb.h>

using namespace tox;


struct tox::NetworkAddressData
{
  struct addrinfo *const info;

  NetworkAddressData (struct addrinfo *info)
    : info (info)
  {
    assert (info != nullptr);
  }

  ~NetworkAddressData ()
  {
    freeaddrinfo (info);
  }
};


NetworkAddress::NetworkAddress (pointer &&d)
  : PrivateType (std::move (d))
{
}


NetworkAddress::NetworkAddress (NetworkAddress &&rhs)
  : PrivateType (std::move (rhs))
{
}


NetworkAddress::~NetworkAddress ()
{
}



Partial<NetworkAddress>
NetworkAddress::resolve (char const *node)
{
  if (node == nullptr)
    {
      LOG (WARNING) << "Cannot resolve null address";
      return failure ();
    }

  LOG (INFO) << "Resolving `" << node << "'";
  struct addrinfo *info;
  switch (int error = getaddrinfo (node, nullptr, nullptr, &info))
    {
    case 0:
      return success (NetworkAddress (make_unique<Data> (info)));

    case EAI_ADDRFAMILY:
    case EAI_AGAIN:
    case EAI_BADFLAGS:
    case EAI_FAIL:
    case EAI_FAMILY:
    case EAI_MEMORY:
    case EAI_NODATA:
    case EAI_NONAME:
    case EAI_SERVICE:
    case EAI_SOCKTYPE:
    case EAI_SYSTEM:
      LOG (ERROR) << "Could not resolve `" << node << "': " << gai_strerror (error);
      return failure ();

    default:
      LOG (ERROR) << "Unknown error while resolving `" << node << "': " << gai_strerror (error);
      return failure ();
    }
}
