#pragma once

#include "util/instance_manager.h"
#include "util/pp_cat.h"
#include "util/debug_log.h"

#include <iostream>


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



#define ERROR_CODE(METHOD)                          \
  PP_CAT (SUBSYSTEM, _ERR_##METHOD)

#define success_case(METHOD)                        \
  case PP_CAT (ERROR_CODE (METHOD), _OK):           \
    return success()

#define failure_case(METHOD, ERROR)                 \
  case PP_CAT (ERROR_CODE (METHOD), _##ERROR):      \
    return failure (#ERROR)


#define HANDLE(NAME, METHOD)                                      \
template<>                                                        \
extern char const *const method_name<ERROR_CODE (METHOD)> = NAME; \
                                                                  \
template<>                                                        \
ErrorHandling                                                     \
handle_error_enum<ERROR_CODE (METHOD)> (ERROR_CODE (METHOD) error)


/**
 * Package name inside im.tox.tox4j (av or core) in which the subsystem
 * classes live.
 */
template<typename Subsystem>
extern char const *const module_name;

template<typename Subsystem>
extern char const *const exn_prefix;

template<typename ErrorCode>
extern char const *const method_name;

template<typename Object, typename ErrorType>
void
throw_tox_exception (JNIEnv *env, char const *error)
{
  return throw_tox_exception (env, module_name<Object>, exn_prefix<Object>, method_name<ErrorType>, error);
}

template<typename Object, typename ErrorType>
void
throw_tox_exception (JNIEnv *env, ErrorType error)
{
  ErrorHandling result = handle_error_enum<ErrorType> (error);
  switch (result.result)
    {
    case ErrorHandling::FAILURE:
      return throw_tox_exception<Object, ErrorType> (env, result.error);
    case ErrorHandling::SUCCESS:
      return throw_illegal_state_exception (env, error, "Throwing OK code");
    case ErrorHandling::UNHANDLED:
      return throw_illegal_state_exception (env, error, "Unknown error code");
    }
}


template<typename Object, typename SuccessFunc, typename ToxFunc, typename ...Args>
auto
with_error_handling (LogEntry &log_entry,
                     JNIEnv *env,
                     SuccessFunc success_func,
                     ToxFunc tox_func,
                     Args ...args)
{
  using namespace std::placeholders;

  using error_type = typename error_type_of<ToxFunc>::type;

  using result_type =
    typename std::result_of<
      SuccessFunc (
        typename std::result_of<
          ToxFunc (Args..., error_type *)
        >::type
      )>::type;

  error_type error;
  auto value = log_entry.print_result (tox_func, args..., &error);
  ErrorHandling result = handle_error_enum<error_type> (error);
  switch (result.result)
    {
    case ErrorHandling::SUCCESS:
      return success_func (std::move (value));
    case ErrorHandling::FAILURE:
      throw_tox_exception<Object, error_type> (env, result.error);
      break;
    case ErrorHandling::UNHANDLED:
      throw_illegal_state_exception (env, error, "Unknown error code");
      break;
    }

  return result_type ();
}


template<typename ObjectP, typename EventsP>
struct ToxInstances
  : instance_manager<ObjectP, EventsP>
{
  typedef typename instance_manager<ObjectP, EventsP>::Object Object;
  typedef typename instance_manager<ObjectP, EventsP>::Events Events;

  template<typename SuccessFunc, typename ToxFunc, typename ...Args>
  auto
  with_error_handling (JNIEnv *env,
                       SuccessFunc success_func,
                       ToxFunc tox_func,
                       Args ...args)
  {
    LogEntry log_entry (tox_func, args...);
    return ::with_error_handling<Object> (log_entry, env, success_func, tox_func, args...);
  }


  template<typename SuccessFunc, typename ToxFunc, typename ...Args>
  auto
  with_instance_err (JNIEnv *env,
                     jint instanceNumber,
                     SuccessFunc success_func,
                     ToxFunc tox_func,
                     Args ...args)
  {
    return this->with_instance (env, instanceNumber,
      [=] (Object *tox, Events &events)
        {
          unused (events);
          LogEntry log_entry (instanceNumber, tox_func, tox, args...);
          return ::with_error_handling<Object> (log_entry, env, success_func, tox_func, tox, args...);
        }
    );
  }


  template<typename ToxFunc, typename ...Args>
  auto
  with_instance_ign (JNIEnv *env,
                     jint instanceNumber,
                     ToxFunc tox_func,
                     Args ...args)
  {
    struct ignore
    {
      void operator () (bool) { }
    };
    return with_instance_err (env, instanceNumber, ignore (), tox_func, args...);
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
          LogEntry log_entry (instanceNumber, tox_func, tox, args...);
          return log_entry.print_result (tox_func, tox, args...);
        }
    );
  }
};
