#pragma once

#include <cassert>
#include <cstddef>

#include <array>
#include <tuple>
#include <vector>
#include <type_traits>

#include "Status.h"
#include "tuple_util.h"

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
    : std::false_type
  { };

  template<typename T>
  struct is_partial<Partial<T>>
    : std::true_type
  { };

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
  struct PartialBase
  {
    PartialBase ()
      : status_ (Status::OK)
    { }

    PartialBase (PartialBase const &rhs)
      : status_ (rhs.status_)
    { }

    Status code () const
    { return status_; }

    bool ok () const
    { return status_ == Status::OK; }

  protected:
    PartialBase (Status status)
      : status_ (status)
    { }

  private:
    Status status_;
  };


  template<typename Success>
  struct Partial
    : PartialBase
  {
    Partial (Success const &success)
      : PartialBase (Status::OK)
    {
      new (static_cast<void *> (value_)) Success (success);
    }

    Partial (Status status)
      : PartialBase (status)
    { assert (!ok ()); }

    Partial (Partial const &rhs)
      : PartialBase (rhs)
    {
      if (ok ())
        new (static_cast<void *> (value_)) Success (*rhs.value ());
    }

    ~Partial ()
    {
      if (ok ())
        reinterpret_cast<Success *> (value_)->~Success ();
    }

    template<typename MapF>
    typename partial_type<MapF (Success)>::type
    operator >>= (MapF const &func)
    {
      typedef typename partial_type<MapF (Success)>::type result_type;
      if (ok ())
        return func (*value ());
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
    Success const *value () const
    {
      assert (ok ());
      return reinterpret_cast<Success const *> (value_);
    }

    alignas (Success) char value_[sizeof (Success)];
  };


  template<>
  struct Partial<void>
    : PartialBase
  {
    Partial ()
    { }

    Partial (Status status)
      : PartialBase (status)
    { assert (!ok ()); }

    template<typename VoidF>
    typename partial_type<VoidF ()>::type
    operator >> (VoidF const &func)
    {
      typedef typename partial_type<VoidF ()>::type result_type;
      if (ok ())
        return func ();
      return result_type (code ());
    }
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
