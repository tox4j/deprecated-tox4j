#pragma once

#include "Status.h"

#include <cassert>

#include <type_traits>
#include <utility>


namespace tox
{
  template<typename T, template<typename...> class Template>
  struct is_specialisation_of
    : std::false_type
  { };

  template<template<typename...> class Template, typename... Args>
  struct is_specialisation_of<Template<Args...>, Template>
    : std::true_type
  { };


  template<typename T>
  struct partial_traits
  {
    static void move (T &value, T &&from)
    {
      new (static_cast<void *> (&value)) T (std::move (from));
    }

    static void copy (T &value, T const &from)
    {
      new (static_cast<void *> (&value)) T (from);
    }

    static void destroy (T &value)
    {
      value.~T ();
    }
  };

  template<std::size_t N, typename T>
  struct partial_traits<T[N]>
  {
    static void move (T (&values)[N], T (&&from)[N])
    {
      for (std::size_t i = 0; i < N; i++)
        partial_traits<T>::move (values[i], std::move (from[i]));
    }

    static void copy (T (&values)[N], T const (&from)[N])
    {
      for (std::size_t i = 0; i < N; i++)
        partial_traits<T>::copy (values[i], from[i]);
    }

    static void destroy (T (&values)[N])
    {
      for (std::size_t i = 0; i < N; i++)
        partial_traits<T>::destroy (values[i]);
    }
  };


  template<typename Success, typename Traits = partial_traits<Success>>
  struct Partial;


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
    template<typename Call>
    struct return_type
    {
      typedef typename std::result_of<Call>::type type;

      static_assert (is_specialisation_of<type, Partial>::value,
                     "Monadic bind must return Partial<T>");
    };


    PartialBase (Status status)
      : status_ (status)
    { }


  private:
    Status status_;
  };


  template<typename Success, typename Traits>
  struct Partial
    : PartialBase
  {
    template<typename OtherSuccess, typename OtherTraits>
    friend struct Partial;

    Partial (Success &&success)
      : PartialBase (Status::OK)
    {
      Traits::move (value (), std::move (success));
    }

    Partial (Success const &success)
      : PartialBase (Status::OK)
    {
      Traits::copy (value (), success);
    }

    Partial (Status status)
      : PartialBase (status)
    { assert (!ok ()); }

    Partial (Partial &&rhs)
      : PartialBase (rhs)
    {
      if (ok ())
        Traits::move (value (), std::move (rhs.value ()));
    }

    Partial (Partial const &rhs)
      : PartialBase (rhs)
    {
      if (ok ())
        Traits::copy (value (), rhs.value ());
    }

    template<typename OtherSuccess, typename OtherTraits>
    Partial (Partial<OtherSuccess, OtherTraits> &&rhs)
      : PartialBase (rhs)
    {
      if (ok ())
        Traits::move (value (), std::move (rhs.value ()));
    }

    template<typename OtherSuccess, typename OtherTraits>
    Partial (Partial<OtherSuccess, OtherTraits> const &rhs)
      : PartialBase (rhs)
    {
      if (ok ())
        Traits::copy (value (), rhs.value ());
    }

    ~Partial ()
    {
      if (ok ())
        Traits::destroy (value ());
    }

    template<typename MapF>
    typename return_type<MapF (Success)>::type
    operator >>= (MapF const &func)
    {
      typedef typename return_type<MapF (Success)>::type result_type;
      if (ok ())
        return func (value ());
      return result_type (code ());
    }

    template<typename VoidF>
    typename return_type<VoidF ()>::type
    operator >> (VoidF const &func)
    {
      typedef typename return_type<VoidF ()>::type result_type;
      if (ok ())
        return func ();
      return result_type (code ());
    }

  private:
    Success &value ()
    {
      assert (ok ());
      return *reinterpret_cast<Success *> (value_);
    }

    Success const &value () const
    {
      assert (ok ());
      return *reinterpret_cast<Success const *> (value_);
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
    typename return_type<VoidF ()>::type
    operator >> (VoidF const &func)
    {
      typedef typename return_type<VoidF ()>::type result_type;
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
}
