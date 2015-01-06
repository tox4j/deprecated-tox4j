#pragma once

#include <cassert>
#include <cstddef>

#include <array>
#include <tuple>
#include <vector>
#include <type_traits>

#include "Partial.h"
#include "tuple_util.h"

namespace tox
{
  typedef unsigned char byte;

  template<std::size_t N>
  using byte_array = std::array<byte, N>;

  using byte_vector = std::vector<byte>;


  template<typename T, typename... Args>
  T &
  renew (T &v, Args &&...args)
  {
    v.~T ();
    return *new (static_cast<void *> (&v)) T (args...);
  }


  // Forward declarations.
  struct PublicKey;
  struct SecretKey;
  struct KeyPair;
  struct Nonce;

  struct CryptoBox;

  template<typename MessageFormat>
  struct Message;
  struct PlainText;
  struct CipherText;
  template<typename MessageFormat>
  struct BitStream;

  struct IPv4Address;
  struct IPv6Address;
}
