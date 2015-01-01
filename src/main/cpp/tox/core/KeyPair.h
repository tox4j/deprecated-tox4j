#pragma once

#include "types.h"

#include <sodium.h>


namespace tox
{
  struct PublicKey
    : byte_array<crypto_box_PUBLICKEYBYTES>
  {
  };


  struct SecretKey
    : byte_array<crypto_box_SECRETKEYBYTES>
  {
  };


  struct KeyPair
  {
    KeyPair ();

    PublicKey public_key;
    SecretKey secret_key;
  };
}
