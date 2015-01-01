#include "KeyPair.h"

#include <sodium.h>

using namespace tox;


KeyPair::KeyPair ()
{
  crypto_box_keypair (public_key.data (), secret_key.data ());
}
