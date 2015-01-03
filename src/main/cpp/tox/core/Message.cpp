#include "Message.h"
#include "Logging.h"

using namespace tox;


template<typename MessageFormat>
MessageFormat &
Message<MessageFormat>::operator << (uint8_t  b)
{
  push_back (b & 0xff);
  return static_cast<MessageFormat &> (*this);
}


template<typename MessageFormat>
MessageFormat &
Message<MessageFormat>::operator << (uint16_t s)
{
  push_back ((s >> 8 * 1) & 0xff);
  push_back ((s >> 8 * 0) & 0xff);
  return static_cast<MessageFormat &> (*this);
}


template<typename MessageFormat>
MessageFormat &
Message<MessageFormat>::operator << (uint32_t l)
{
  push_back ((l >> 8 * 3) & 0xff);
  push_back ((l >> 8 * 2) & 0xff);
  push_back ((l >> 8 * 1) & 0xff);
  push_back ((l >> 8 * 0) & 0xff);
  return static_cast<MessageFormat &> (*this);
}


template<typename MessageFormat>
MessageFormat &
Message<MessageFormat>::operator << (uint64_t q)
{
  push_back ((q >> 8 * 7) & 0xff);
  push_back ((q >> 8 * 6) & 0xff);
  push_back ((q >> 8 * 5) & 0xff);
  push_back ((q >> 8 * 4) & 0xff);
  push_back ((q >> 8 * 3) & 0xff);
  push_back ((q >> 8 * 2) & 0xff);
  push_back ((q >> 8 * 1) & 0xff);
  push_back ((q >> 8 * 0) & 0xff);
  return static_cast<MessageFormat &> (*this);
}


template<typename MessageFormat>
MessageFormat &
Message<MessageFormat>::operator << (MessageFormat const &message)
{
  append (message.cbegin (), message.cend ());
  return static_cast<MessageFormat &> (*this);
}


template<typename MessageFormat>
MessageFormat &
Message<MessageFormat>::operator << (PublicKey const &key)
{
  append (key.cbegin (), key.cend ());
  return static_cast<MessageFormat &> (*this);
}


template<typename MessageFormat>
MessageFormat &
Message<MessageFormat>::operator << (Nonce const &nonce)
{
  append (nonce.cbegin (), nonce.cend ());
  return static_cast<MessageFormat &> (*this);
}


template struct tox::Message<PlainText>;
template struct tox::Message<CipherText>;
