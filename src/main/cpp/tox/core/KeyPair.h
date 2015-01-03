#pragma once

#include "types.h"

#include <sodium.h>


namespace tox
{
  struct PublicKey
    : byte_array<crypto_box_PUBLICKEYBYTES>
  {
    typedef byte_array<crypto_box_PUBLICKEYBYTES> super;

    PublicKey ()
    { }

    PublicKey (byte const (&key)[crypto_box_PUBLICKEYBYTES])
    {
      std::copy (key, key + crypto_box_PUBLICKEYBYTES, begin ());
    }
  };


  struct SecretKey
    : byte_array<crypto_box_SECRETKEYBYTES>
  {
    typedef byte_array<crypto_box_SECRETKEYBYTES> super;
  };


  struct KeyPair
  {
    KeyPair ();

    PublicKey public_key;
    SecretKey secret_key;
  };
}
