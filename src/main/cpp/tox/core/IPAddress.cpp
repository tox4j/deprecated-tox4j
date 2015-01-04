#include "IPAddress.h"
#include "Logging.h"

using namespace tox;


template<typename MessageFormat>
MessageFormat &
Message<MessageFormat>::operator << (IPv4Address const &address)
{
  append (address.cbegin (), address.cend ());
  return static_cast<MessageFormat &> (*this);
}

template<typename MessageFormat>
MessageFormat &
Message<MessageFormat>::operator << (IPv6Address const &address)
{
  append (address.cbegin (), address.cend ());
  return static_cast<MessageFormat &> (*this);
}

template struct tox::Message<PlainText>;
template struct tox::Message<CipherText>;
