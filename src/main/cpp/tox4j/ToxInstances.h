#pragma once

#include "util/instance_manager.h"
#include "util/pp_cat.h"

/*****************************************************************************
 * Identity and unused-value function.
 */

static auto const identity = [](auto v) { return v; };

template<typename T> static inline void unused (T const &) { }


/*****************************************************************************
 * Error handling code.
 */

struct ErrorHandling
{
  enum Result
  {
    UNHANDLED,
    SUCCESS,
    FAILURE
  };

  Result const result;
  char const *const error;
};

static inline ErrorHandling
success ()
{
  return ErrorHandling { ErrorHandling::SUCCESS, nullptr };
}

static inline ErrorHandling
failure (char const *error)
{
  return ErrorHandling { ErrorHandling::FAILURE, error };
}

static inline ErrorHandling
unhandled ()
{
  return ErrorHandling { ErrorHandling::UNHANDLED, nullptr };
}


template<typename ErrorT>
ErrorHandling
handle_error_enum (ErrorT error);


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



#define ERROR_CODE(METHOD)                          \
  PP_CAT (SUBSYSTEM, _ERR_##METHOD)

#define success_case(METHOD)                        \
  case PP_CAT (ERROR_CODE (METHOD), _OK):           \
    return success()

#define failure_case(METHOD, ERROR)                 \
  case PP_CAT (ERROR_CODE (METHOD), _##ERROR):      \
    return failure (#ERROR)


#define HANDLE(METHOD)                                            \
template<>                                                        \
ErrorHandling                                                     \
handle_error_enum<ERROR_CODE (METHOD)> (ERROR_CODE (METHOD) error)


/**
 * Package name inside im.tox.tox4j (av or core) in which the subsystem
 * classes live.
 */
template<typename Subsystem>
extern char const *const module_name;


template<typename ObjectP, typename EventsP>
struct ToxInstances
  : instance_manager<ObjectP, EventsP>
{
  typedef typename instance_manager<ObjectP, EventsP>::Object Object;
  typedef typename instance_manager<ObjectP, EventsP>::Events Events;

  template<typename SuccessFunc, typename ToxFunc, typename ...Args>
  auto
  with_error_handling (JNIEnv *env,
                       char const *method,
                       SuccessFunc success_func,
                       ToxFunc tox_func,
                       Args ...args)
  {
    using result_type =
      typename std::result_of<
        SuccessFunc (
          typename std::result_of<
            ToxFunc (Args..., tox_error_t<ToxFunc> *)
          >::type
        )>::type;

    tox_error_t<ToxFunc> error;
    auto value = tox_func (args..., &error);
    ErrorHandling result = handle_error_enum<tox_error_t<ToxFunc>> (error);
    switch (result.result)
      {
      case ErrorHandling::SUCCESS:
        return success_func (std::move (value));
      case ErrorHandling::FAILURE:
        throw_tox_exception (env, module_name<Object>, method, result.error);
        break;
      case ErrorHandling::UNHANDLED:
        throw_illegal_state_exception (env, error, "Unknown error code");
        break;
      }

    return result_type ();
  }


  template<typename SuccessFunc, typename ToxFunc, typename ...Args>
  auto
  with_instance_err (JNIEnv *env,
                     jint instanceNumber,
                     char const *method,
                     SuccessFunc success_func,
                     ToxFunc tox_func,
                     Args ...args)
  {
    return this->with_instance (env, instanceNumber,
      [=] (Object *tox, Events &events)
        {
          unused (events);
          return with_error_handling (env, method, success_func, tox_func, tox, args...);
        }
    );
  }


  template<typename ToxFunc, typename ...Args>
  auto
  with_instance_ign (JNIEnv *env,
                     jint instanceNumber,
                     char const *method,
                     ToxFunc tox_func,
                     Args ...args)
  {
    struct ignore
    {
      void operator () (bool) { }
    };
    return with_instance_err (env, instanceNumber, method, ignore (), tox_func, args...);
  }


  template<typename ToxFunc, typename ...Args>
  auto
  with_instance_noerr (JNIEnv *env,
                       jint instanceNumber,
                       ToxFunc tox_func,
                       Args ...args)
  {
    return this->with_instance (env, instanceNumber,
      [&] (Object *tox, Events &events)
        {
          unused (events);
          return tox_func (tox, args...);
        }
    );
  }
};
