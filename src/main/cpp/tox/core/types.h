#pragma once

#include <cassert>
#include <cstddef>

#include <array>
#include <tuple>
#include <vector>

#include "Status.h"

namespace tox
{
  typedef unsigned char byte;

  template<std::size_t N>
  using byte_array = std::array<byte, N>;

  using byte_vector = std::vector<byte>;

  template<typename Success>
  struct Partial;

  template<typename T>
  struct is_partial
  {
    static bool const value = false;
  };

  template<typename T>
  struct is_partial<Partial<T>>
  {
    static bool const value = true;
  };

  template<typename Call>
  struct partial_type
  {
    typedef typename std::result_of<Call>::type type;

    static_assert (is_partial<type>::value,
                   "Monadic bind must return Partial<T>");
  };

  /**
   * The type of partial functions.
   *
   * A partial function is a function that does not have a valid output for
   * every possible input. In case an input was provided for which no valid
   * output can be produced, the function can return an error code.
   */
  template<typename Success>
  struct Partial
  {
    Partial (Success const &success)
      : status_ (Status::OK)
    {
      new (static_cast<void *> (value_)) Success (success);
    }

    Partial (Partial const &rhs)
      : status_ (rhs.status_)
    {
      if (ok ())
        new (static_cast<void *> (value_)) Success (*reinterpret_cast<Success const *> (rhs.value_));
    }

    ~Partial ()
    {
      if (ok ())
        reinterpret_cast<Success *> (value_)->~Success ();
    }

    Partial (Status status)
      : status_ (status)
    { assert (!ok ()); }

    Status code () const
    { return status_; }

    bool ok () const
    { return status_ == Status::OK; }

    template<typename MapF>
    typename partial_type<MapF (Success)>::type
    operator >>= (MapF const &func)
    {
      typedef typename partial_type<MapF (Success)>::type result_type;
      if (ok ())
        return func (value ());
      return result_type (code ());
    }

    template<typename VoidF>
    typename partial_type<VoidF ()>::type
    operator >> (VoidF const &func)
    {
      typedef typename partial_type<VoidF ()>::type result_type;
      if (ok ())
        return func ();
      return result_type (code ());
    }

  private:
    Success value () const
    {
      assert (ok ());
      return *reinterpret_cast<Success const *> (value_);
    }

    Status status_;
    alignas (Success) char value_[sizeof (Success)];
  };


  template<>
  struct Partial<void>
  {
    Partial ()
      : status_ (Status::OK)
    { }

    Partial (Partial const &rhs)
      : status_ (rhs.status_)
    { }

    ~Partial ()
    { }

    Partial (Status status)
      : status_ (status)
    { assert (!ok ()); }

    Status code () const
    { return status_; }

    bool ok () const
    { return status_ == Status::OK; }

    template<typename MapF>
    typename partial_type<MapF ()>::type
    operator >>= (MapF const &func)
    {
      // Map function doesn't get any argument, so it's the same as the
      // ignore-result operator >>.
      return *this >> func;
    }

    template<typename VoidF>
    typename partial_type<VoidF ()>::type
    operator >> (VoidF const &func)
    {
      typedef typename partial_type<VoidF ()>::type result_type;
      if (ok ())
        return func ();
      return result_type (code ());
    }

  private:
    Status status_;
  };


  struct failure
  {
    failure (Status status = Status::Unknown)
      : status_ (status)
    { }

    template<typename Success>
    operator Partial<Success> () const
    {
      return Partial<Success> (status_);
    }

  private:
    Status const status_;
  };


  template<typename Success>
  Partial<Success>
  success (Success const &success)
  {
    return Partial<Success> (success);
  }

  static inline Partial<void>
  success ()
  {
    return Partial<void> ();
  }

  // Forward declarations.
  struct KeyPair;
  struct PublicKey;
  struct SecretKey;
  struct Nonce;
  struct PlainText;
  struct CipherText;
  template<typename MessageFormat>
  struct BitStream;
  struct CryptoBox;
  struct IPv4Address;
  struct IPv6Address;
}
