#include <tox/core.h>

// XXX: bunslow broke the API; fix it here
extern "C" void tox_self_get_private_key(const Tox *tox, uint8_t *secret_key);

void
tox_self_get_secret_key (Tox const *tox, uint8_t *secret_key)
{
  return tox_self_get_private_key (tox, secret_key);
}
