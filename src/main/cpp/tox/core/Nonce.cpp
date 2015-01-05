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


template<typename MessageFormat>
BitStream<MessageFormat>
BitStream<MessageFormat>::operator >> (Nonce &nonce) const
{
  assert (position_ % 8 == 0);
  assert (packet_.size () > position_ / 8 + nonce.size ());
#if 0
  std::copy (cbegin (),
             cbegin () + nonce.size (),
             nonce.begin ());
#endif
  return { position_ + nonce.size () * 8, packet_ };
}

template struct tox::BitStream<PlainText>;
template struct tox::BitStream<CipherText>;
