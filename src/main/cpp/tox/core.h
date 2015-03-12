#pragma once

// XXX: Fix invalid names in tox.h.
#define TOX_ERR_PROXY_TYPE TOX_ERR_NEW_PROXY_BAD_TYPE
#define TOX_ERR_FILE_SEND_CHUNK_QUEUE_FULL TOX_ERR_FILE_SEND_CHUNK_SENDQ

#include <tox/tox.h>

#ifdef __cplusplus
extern "C" {
#endif

/**
 * The size of a Tox Secret Key in bytes.
 */
#define TOX_SECRET_KEY_SIZE             32

/**
 * Copy the secret key from the Tox object.
 *
 * @param secret_key A memory region of at least TOX_SECRET_KEY_SIZE bytes. If
 *   this parameter is NULL, this function has no effect.
 */
void tox_self_get_secret_key(Tox const *tox, uint8_t *secret_key);

#ifdef __cplusplus
}
#endif
