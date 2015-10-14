#include "ToxCrypto.h"

#include <tox/tox.h>
#define TOX_PASS_HASH_LENGTH TOX_HASH_LENGTH
#include "generated/constants.h"

template<> extern char const *const module_name<ToxCrypto> = "crypto";
template<> extern char const *const exn_prefix<ToxCrypto> = "";
