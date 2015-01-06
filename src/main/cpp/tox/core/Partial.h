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
  struct identity
  { typedef T type; };


  template<typename To>
  To
  implicit_cast (typename identity<To>::type const &from)
  {
    return from;
  }

  template<typename To, typename From>
  To
  pointer_cast (From *from)
  {
    return static_cast<To> (implicit_cast<void *> (from));
  }

  template<typename To, typename From>
  To
  pointer_cast (From const *from)
  {
    return static_cast<To> (implicit_cast<void const *> (from));
  }


  template<typename T>
  struct partial_traits
  {
    typedef typename std::remove_const<T>::type       &      reference;
    typedef typename std::remove_const<T>::type const &const_reference;
    typedef typename std::remove_const<T>::type       *      pointer;
    typedef typename std::remove_const<T>::type const *const_pointer;

    static void move (reference value, T &&from)
    {
      new (static_cast<void *> (&value)) T (std::move (from));
    }

    static void copy (reference value, T const &from)
    {
      new (static_cast<void *> (&value)) T (from);
    }

    static void destroy (reference value)
    {
      value.~T ();
    }
  };

  template<std::size_t N, typename T>
  struct partial_traits<T[N]>
  {
    typedef typename std::remove_const<T>::type       (&      reference)[N];
    typedef typename std::remove_const<T>::type const (&const_reference)[N];
    typedef typename std::remove_const<T>::type       (*      pointer  )[N];
    typedef typename std::remove_const<T>::type const (*const_pointer  )[N];

    static void move (reference values, T (&&from)[N])
    {
      for (std::size_t i = 0; i < N; i++)
        partial_traits<T>::move (values[i], std::move (from[i]));
    }

    static void copy (reference values, T const (&from)[N])
    {
      for (std::size_t i = 0; i < N; i++)
        partial_traits<T>::copy (values[i], from[i]);
    }

    static void destroy (reference values)
    {
      for (std::size_t i = 0; i < N; i++)
        partial_traits<T>::destroy (values[i]);
    }
  };


  template<typename Success, typename Traits = partial_traits<typename std::remove_reference<Success>::type>>
  struct Partial;

  struct failure;


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

      static_assert (is_specialisation_of<type, Partial>::value ||
                     std::is_same<type, failure>::value,
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

    typedef typename Traits::      reference       reference;
    typedef typename Traits::const_reference const_reference;
    typedef typename Traits::      pointer         pointer;
    typedef typename Traits::const_pointer   const_pointer;

    Partial (Success &&success)
      : PartialBase (Status::OK)
    {
      Traits::move (value (), std::move (success));
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
    typename std::result_of<MapF (Success)>::type
    operator ->* (MapF const &func)
    {
      typedef typename return_type<MapF (Success)>::type result_type;
      if (ok ())
        return func (value ());
      return result_type (code ());
    }

    template<typename VoidF>
    typename std::result_of<VoidF ()>::type
    operator ->* (VoidF const &func)
    {
      typedef typename return_type<VoidF ()>::type result_type;
      if (ok ())
        return func ();
      return result_type (code ());
    }

  private:
    reference value ()
    {
      assert (ok ());
      return *pointer_cast<pointer> (value_);
    }

    const_reference value () const
    {
      assert (ok ());
      return *pointer_cast<const_pointer> (value_);
    }

    alignas (Success) unsigned char value_[sizeof (Success)];
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
    typename std::result_of<VoidF ()>::type
    operator ->* (VoidF const &func)
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
  success (Success &&success)
  {
    return Partial<Success> (std::forward<Success> (success));
  }

  static inline Partial<void>
  success ()
  {
    return Partial<void> ();
  }
}
