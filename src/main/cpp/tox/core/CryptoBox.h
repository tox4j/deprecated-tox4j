#pragma once

#include <sodium.h>

#include "KeyPair.h"
#include "Nonce.h"
#include "Message.h"


namespace tox
{
  struct CryptoBox
  {
    CryptoBox (KeyPair const &pair)
      : CryptoBox (pair.public_key, pair.secret_key)
    {
    }

    CryptoBox (PublicKey const &public_key, SecretKey const &secret_key);

    CipherText encrypt (PlainText const &plain, Nonce const &n) const;
    Partial<PlainText> decrypt (CipherText const &crypto, Nonce const &n) const;

  private:
    byte_array<crypto_box_BEFORENMBYTES> shared_key_;
  };
}
