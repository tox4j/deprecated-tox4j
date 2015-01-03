#include "KeyPair.h"
#include "Logging.h"

#include "Message.h"

#include <sodium.h>

using namespace tox;


KeyPair::KeyPair ()
{
  crypto_box_keypair (public_key.data (), secret_key.data ());
}


template<typename MessageFormat>
MessageFormat &
Message<MessageFormat>::operator << (PublicKey const &key)
{
  append (key.cbegin (), key.cend ());
  return static_cast<MessageFormat &> (*this);
}

template struct tox::Message<PlainText>;
template struct tox::Message<CipherText>;
