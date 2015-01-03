#pragma once

#include "tox/core/types.h"

#include <sodium.h>


namespace tox
{
  struct Nonce
    : byte_array<crypto_box_NONCEBYTES>
  {
    using byte_array<crypto_box_NONCEBYTES>::byte_array;

    static Nonce random ();

    Nonce &operator++ ();
  };


  struct UniqueNonce
  {
    UniqueNonce ();

    Nonce next ();

  private:
    Nonce next_;

    UniqueNonce &operator= (UniqueNonce const &rhs) = delete;
    UniqueNonce (UniqueNonce const &rhs) = delete;
  };
}
