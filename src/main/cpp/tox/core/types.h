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

    Success value () const
    {
      assert (ok ());
      return *reinterpret_cast<Success const *> (value_);
    }

  private:
    Status status_;
    alignas (Success) char value_[sizeof (Success)];
  };


  struct failure
  {
    failure (Status status)
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


  template<std::size_t...>
  struct seq { };

  template<std::size_t N, std::size_t ...S>
  struct make_seq_t : make_seq_t<N - 1, N - 1, S...> { };

  template<std::size_t ...S>
  struct make_seq_t<0, S...>
  { typedef seq<S...> type; };


  template<std::size_t N>
  using make_seq = typename make_seq_t<N>::type;


  template<typename... Args>
  struct success_t
  {
    success_t (Args const &...args)
      : args_ (args...)
    { }

    template<typename Success>
    operator Partial<Success> () const
    {
      return Partial<Success> (make_success<Success> (make_seq<sizeof... (Args)> ()));
    }

  private:
    template<typename Success, std::size_t ...S>
    Success make_success (seq<S...>) const
    {
      return Success (std::get<S> (args_)...);
    }

    std::tuple<Args...> const args_;
  };

  template<typename... Args>
  success_t<Args...>
  success (Args const &...args)
  {
    return success_t<Args...> (args...);
  }
}
