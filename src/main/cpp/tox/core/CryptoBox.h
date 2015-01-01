#pragma once

#include <string>

#include <sodium.h>

#include "KeyPair.h"
#include "Nonce.h"
#include "types.h"


namespace tox
{
  struct Message
    : byte_vector
  {
    using byte_vector::byte_vector;
  };


  struct PlainText
    : Message
  {
    using Message::Message;

    explicit PlainText (std::string const &text)
      : Message (text.begin (), text.end ())
    {
    }

    std::string str () const
    {
      return std::string (begin (), end ());
    }
  };


  struct CipherText
    : Message
  {
    using Message::Message;
  };



  struct CryptoBox
  {
    CryptoBox (KeyPair const &pair)
      : CryptoBox (pair.public_key, pair.secret_key)
    {
    }

    CryptoBox (PublicKey const &public_key, SecretKey const &secret_key);

    CipherText encrypt (PlainText const &plain, Nonce const &n);
    Partial<PlainText> decrypt (CipherText const &crypto, Nonce const &n);

  private:
    byte_array<crypto_box_BEFORENMBYTES> shared_key_;
  };
}
