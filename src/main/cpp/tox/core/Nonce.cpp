#include "Nonce.h"
#include "Logging.h"

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
