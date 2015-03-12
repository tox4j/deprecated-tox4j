#include <tox/core.h>

void
tox_self_get_secret_key (Tox const *tox, uint8_t *secret_key)
{
  return tox_self_get_private_key (tox, secret_key);
}
