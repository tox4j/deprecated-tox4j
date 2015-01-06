#pragma once

#include <cassert>
#include <cstddef>

#include <array>
#include <memory>
#include <tuple>
#include <vector>
#include <type_traits>

#include "Partial.h"
#include "tuple_util.h"

#if defined(__GNUC__)
#  define DEPRECATED(message) __attribute__ ((__deprecated__ (message)))
#else
#  define DEPRECATED(message)
#endif

namespace tox
{
  typedef unsigned char byte;

  template<std::size_t N>
  using byte_array = std::array<byte, N>;

  using byte_vector = std::vector<byte>;


  bool crypto_equal (byte const *a, byte const *b, std::size_t length);
  void crypto_memzero (byte *a, std::size_t length);


  template<std::size_t N>
  struct crypto_byte_array
    : byte_array<N>
  {
    using byte_array<N>::byte_array;

    void clear ()
    { crypto_memzero (this->data (), this->size ()); }

    bool operator == (crypto_byte_array const &rhs) const
    { return crypto_equal (this->data (), rhs.data (), this->size ()); }

    bool operator != (crypto_byte_array const &rhs) const
    { return !(*this == rhs); }

    DEPRECATED ("Lexicographical comparison of crypto_byte_array is not supported")
    bool operator <  (crypto_byte_array const &rhs) const;

    DEPRECATED ("Lexicographical comparison of crypto_byte_array is not supported")
    bool operator >  (crypto_byte_array const &rhs) const;

    DEPRECATED ("Lexicographical comparison of crypto_byte_array is not supported")
    bool operator <= (crypto_byte_array const &rhs) const;

    DEPRECATED ("Lexicographical comparison of crypto_byte_array is not supported")
    bool operator >= (crypto_byte_array const &rhs) const;
  };


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


  enum class Protocol
    : byte
  {
    UDP = 0,
    TCP = 1,
  };


  template<typename DataType>
  struct PrivateType
  {
    typedef DataType Data;
    typedef std::unique_ptr<Data> pointer;

    Data       &data ()       { return *d; }
    Data const &data () const { return *d; }

    explicit PrivateType (pointer &&d) : d (std::move (d)) { }
    PrivateType (PrivateType &&rhs) : d (std::move (rhs.d)) { }
    ~PrivateType () { }

  protected:
    pointer d;
  };

  template<typename T, typename ...Args>
  std::unique_ptr<T>
  make_unique (Args &&...args)
  {
    return std::unique_ptr<T> (new T (std::forward<Args> (args)...));
  }
}
