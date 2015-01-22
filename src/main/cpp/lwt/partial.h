#pragma once

#include "variant.h"

namespace lwt
{
  template<typename Success, typename Failure>
  struct traits
  {
    static Failure convert_failure (Failure failure) { return failure; }
    static Success convert_success (Success &&success) { return std::move (success); }
    static Success convert_success (Success const &success) { return success; }
  };

  template<typename Failure>
  struct traits<void, Failure>
  {
    static Failure convert_failure (Failure failure) { return failure; }
  };


  template<typename Success, typename Failure, typename Traits>
  class partial_t
  {
    template<typename, typename, typename>
    friend class partial_t;

  public:
    typedef Success success_type;
    typedef Failure failure_type;
    typedef variant<Success, Failure> value_type;

    explicit partial_t (Success &&success)
      : value_ (std::move (success))
    { }

    explicit partial_t (Success const &success)
      : value_ (success)
    { }

    explicit partial_t (Failure failure)
      : value_ (failure)
    { }

    template<typename RSuccess, typename RFailure, typename RTraits>
    partial_t (partial_t<RSuccess, RFailure, RTraits> &&rhs)
      : value_ (convert (std::move (rhs.value_)))
    { }


  protected:
    template<typename RSuccess, typename RFailure>
    static value_type convert (variant<RSuccess, RFailure> &&rhs)
    {
      return std::move (rhs).match (
        [] (RSuccess &&success)
        { return value_type (Traits::convert_success (std::move (success))); },

        [] (RFailure failure)
        { return value_type (Traits::convert_failure (failure)); }
      );
    }

    partial_t &operator = (partial_t const &rhs) = delete;

    value_type value_;
  };


  template<typename Failure, typename Traits>
  class partial_t<void, Failure, Traits>
  {
    struct Success { };

  public:
    typedef Success success_type;
    typedef Failure failure_type;

    template<typename, typename, typename>
    friend class partial_t;

    partial_t ()
      : value_ (Success ())
    { }

    explicit partial_t (Failure failure)
      : value_ (failure)
    { }


  protected:
    partial_t &operator = (partial_t const &rhs) = delete;

    variant<Success, Failure> value_;
  };


  template<typename Success, typename Failure, typename Traits = traits<Success, Failure>>
  struct partial
    : partial_t<Success, Failure, Traits>
  {
    using partial_t<Success, Failure, Traits>::partial_t;

    using success_type = typename partial_t<Success, Failure, Traits>::success_type;
    using failure_type = typename partial_t<Success, Failure, Traits>::failure_type;


    bool ok () const { return this->value_.template is<success_type> (); }


    failure_type code () const
    {
      return this->value_.match (
        [] (success_type const &/*success*/) -> failure_type
        { assert (!"Requested error code from success value"); },

        [] (failure_type const &failure) { return failure; }
      );
    }


    template<typename MapF>
    typename std::result_of<MapF (success_type)>::type
    bind (MapF const &func) const
    {
      typedef typename std::result_of<MapF (success_type)>::type result_type;

      return this->value_.match (
        [&] (success_type const &success) { return func (success); },
        [] (failure_type const &failure) { return result_type (failure); }
      );
    }


    template<typename MapF>
    typename std::result_of<MapF ()>::type
    bind (MapF const &func) const
    {
      typedef typename std::result_of<MapF ()>::type result_type;

      return this->value_.match (
        [&] (success_type const &/*success*/) { return func (); },
        [] (failure_type const &failure) { return result_type (failure); }
      );
    }


    template<typename MapF>
    partial<typename std::result_of<MapF (success_type)>::type, failure_type, Traits>
    map (MapF const &func)
    {
      return bind ([&](success_type const &success) {
        return partial<typename std::result_of<MapF (success_type)>::type, failure_type, Traits> (func (success));
      });
    }


    template<typename MapF>
    auto operator ->* (MapF const &func)
      -> decltype (this->template bind<MapF> (func))
    {
      return bind (func);
    }
  };


  template<typename Success, typename Failure>
  partial<typename std::decay<Success>::type, Failure>
  success (Success &&v)
  {
    return partial<typename std::decay<Success>::type, Failure> (std::forward<Success> (v));
  }

  template<typename Failure>
  inline partial<void, Failure>
  success ()
  {
    return partial<void, Failure> ();
  }


  template<typename Failure>
  struct failure_t
  {
    Failure failure_;

    failure_t (Failure failure)
      : failure_ (failure)
    { }

    template<typename Success, typename RFailure, typename Traits>
    operator partial<Success, RFailure, Traits> () const
    {
      return partial<Success, RFailure, Traits> (Traits::convert_failure (failure_));
    }
  };

  template<typename Failure>
  failure_t<Failure>
  failure (Failure v = traits<void, Failure>::failure)
  {
    return failure_t<Failure> { v };
  }
}


#define DEFINE_PARTIAL_TYPE(Partial, Failure, DefaultFailure)           \
  template<typename Success>                                            \
  using Partial = ::lwt::partial<Success, Failure>;                     \
                                                                        \
  static inline ::lwt::failure_t<Failure>                               \
  failure (Failure v = DefaultFailure)                                  \
  {                                                                     \
    return ::lwt::failure_t<Failure> { v };                             \
  }                                                                     \
                                                                        \
  static inline Partial<void>                                           \
  success ()                                                            \
  {                                                                     \
    return ::lwt::success<Failure> ();                                  \
  }                                                                     \
                                                                        \
  template<typename Success>                                            \
  Partial<typename std::decay<Success>::type>                           \
  success (Success &&v)                                                 \
  {                                                                     \
    return ::lwt::success<Success, Failure> (std::forward<Success> (v));\
  }
