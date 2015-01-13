#include "KeyPair.h"
#include "lwt/logging.h"

#include "Message.h"

#include <sodium.h>

using namespace tox;


KeyPair::KeyPair ()
{
  crypto_box_keypair (public_key.data (), secret_key.data ());
}
