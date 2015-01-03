#include "Nonce.h"
#include "Logging.h"

#include "Message.h"

#include <sodium.h>

using namespace tox;


Nonce Nonce::random ()
{
  Nonce nonce;
  randombytes_buf (nonce.data (), nonce.size ());
  return nonce;
}


Nonce &
Nonce::operator++ ()
{
  for (size_t i = size (); i != 0; i--)
    if (++(*this)[i - 1] != 0)
      break;
  return *this;
}


UniqueNonce::UniqueNonce ()
  : next_ ()
{
}


Nonce
UniqueNonce::next ()
{
  Nonce next = next_;
  ++next_;
  return next;
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
