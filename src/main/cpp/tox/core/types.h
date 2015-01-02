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
    {
      assert (!ok ());
    }

    Status code () const
    { return status_; }

    bool ok () const
    { return status_ == Status::OK; }

    template<typename MapF>
    typename std::result_of<MapF (Success)>::type
    operator >>= (MapF const &func)
    {
      if (ok ())
        return func (value ());
      return typename std::result_of<MapF (Success)>::type (code ());
    }

    template<typename VoidF>
    typename std::result_of<VoidF ()>::type
    operator >> (VoidF const &func)
    {
      if (ok ())
        return func ();
      return typename std::result_of<VoidF ()>::type (code ());
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

  static inline Partial<bool>
  success ()
  {
    return Partial<bool> (true);
  }
}
