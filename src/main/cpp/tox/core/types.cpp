#include "types.h"

#include <sodium.h>

using namespace tox;


bool
tox::crypto_equal (byte const *a, byte const *b, std::size_t length)
{
  return sodium_memcmp (a, b, length);
}

void
tox::crypto_memzero (byte *a, std::size_t length)
{
  sodium_memzero (a, length);
}
