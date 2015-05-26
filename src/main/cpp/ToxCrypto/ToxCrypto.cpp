#include "ToxCrypto.h"


template<>
extern char const *const module_name<ToxCrypto> = "crypto";


HANDLE ("KeyDerivation", KEY_DERIVATION)
{
  switch (error)
    {
    success_case (KEY_DERIVATION);
    failure_case (KEY_DERIVATION, NULL);
    failure_case (KEY_DERIVATION, FAILED);
    }
  return unhandled ();
}


HANDLE ("Encryption", ENCRYPTION)
{
  switch (error)
    {
    success_case (ENCRYPTION);
    failure_case (ENCRYPTION, NULL);
    failure_case (ENCRYPTION, KEY_DERIVATION_FAILED);
    failure_case (ENCRYPTION, FAILED);
    }
  return unhandled ();
}


HANDLE ("Decryption", DECRYPTION)
{
  switch (error)
    {
    success_case (DECRYPTION);
    failure_case (DECRYPTION, NULL);
    failure_case (DECRYPTION, INVALID_LENGTH);
    failure_case (DECRYPTION, BAD_FORMAT);
    failure_case (DECRYPTION, KEY_DERIVATION_FAILED);
    failure_case (DECRYPTION, FAILED);
    }
  return unhandled ();
}
