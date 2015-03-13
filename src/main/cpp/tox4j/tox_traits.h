#pragma once

#include <type_traits>


/*****************************************************************************
 * Find Tox error code (last parameter).
 */

template<typename FuncT>
struct error_type_of;

template<typename Result, typename Head, typename... Tail>
struct error_type_of<Result (*) (Head, Tail...)>
{
  typedef typename error_type_of<Result (*) (Tail...)>::type type;
};

template<typename Result, typename ErrorCode>
struct error_type_of<Result (*) (ErrorCode *)>
{
  typedef ErrorCode type;
};


template<typename FuncT>
using tox_error_t = typename error_type_of<FuncT>::type;


/*****************************************************************************
 * Find result type of success function.
 */

template<typename SuccessFunc, typename ToxFunc, typename ...Args>
using tox_success_t =
  typename std::result_of<
    SuccessFunc (
      typename std::result_of<
        ToxFunc (Args..., tox_error_t<ToxFunc> *)
      >::type
    )
  >::type;


/*****************************************************************************
 * Extract various pieces of information from the success function.
 */

template<typename Func>
struct tox_handler_traits
  : tox_handler_traits<decltype (&Func::operator ())>
{ };


template<typename T, typename R, typename Subsystem, typename Events>
struct tox_handler_traits<R (T::*) (Subsystem *, Events &) const>
{
  typedef R         return_type;
  typedef Subsystem subsystem_type;
  typedef Events    events_type;
};


/*****************************************************************************
 * Extract the subsystem type (Tox, ToxAV, ...) from the Tox API function.
 */

template<typename Func>
struct tox_fun_traits;

template<typename R, typename Subsystem, typename ...Args>
struct tox_fun_traits<R (*) (Subsystem *, Args...)>
{
  typedef Subsystem subsystem_type;
};

template<typename R, typename Subsystem, typename ...Args>
struct tox_fun_traits<R (*) (Subsystem const *, Args...)>
{
  typedef Subsystem subsystem_type;
};
