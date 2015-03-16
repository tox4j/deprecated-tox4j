#pragma once

#include <jni.h>

#include <sstream>


void cosmic_ray_error (char const *function);

void throw_tox_killed_exception (JNIEnv *env, jint instance_number, char const *message);
void throw_illegal_state_exception (JNIEnv *env, jint instance_number, char const *message);
void throw_illegal_state_exception (JNIEnv *env, jint instance_number, std::string const &message);
void throw_tox_exception (JNIEnv *env, char const *module, char const *method, char const *code);

#include "ToxInstances.h"
#include "tox_traits.h"


/*****************************************************************************
 * Identity and ignore value function.
 */

static auto const identity = [](auto v) { return v; };

struct ignore
{
  void operator () (bool) { }
};


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

#define success_case(METHOD)                        \
  case CAT (SUBSYSTEM, _ERR_##METHOD##_OK):         \
    return success()

#define failure_case(METHOD, ERROR)                 \
  case CAT (SUBSYSTEM, _ERR_##METHOD##_##ERROR):    \
    return failure (#ERROR)


template<typename Func>
typename tox_handler_traits<Func>::return_type
with_instance (JNIEnv *env, jint instance_number, Func func)
{
  typedef typename tox_handler_traits<Func>::return_type    return_type;
  typedef typename tox_handler_traits<Func>::subsystem_type subsystem_type;
  typedef typename tox_handler_traits<Func>::events_type    events_type;

  if (instance_number == 0)
    {
      throw_illegal_state_exception (env, instance_number,
          "Function called on incomplete object");
      return return_type ();
    }

  auto &manager = instance_manager<subsystem_type>::self;
  auto lock = manager.lock ();
  if (!manager.isValid (instance_number))
    {
      throw_tox_killed_exception (env, instance_number,
          "Tox function invoked on invalid tox instance");
      return return_type ();
    }

  auto const &instance = manager[instance_number];

  if (!instance.isLive ())
    {
      throw_tox_killed_exception (env, instance_number,
          "Tox function invoked on killed tox instance");
      return return_type ();
    }

  return instance.with_lock ([&lock, &func] (subsystem_type *tox, events_type &events)
    {
      lock.unlock ();
      return func (tox, events);
    }
  );
}


template<typename Subsystem, typename ErrorFunc, typename SuccessFunc, typename ToxFunc, typename ...Args>
tox_success_t<SuccessFunc, ToxFunc, Args...>
with_error_handling (JNIEnv *env,
                     char const *method,
                     ErrorFunc error_func,
                     SuccessFunc success_func,
                     ToxFunc tox_func,
                     Args ...args)
{
  tox_error_t<ToxFunc> error;
  auto value = tox_func (args..., &error);
  ErrorHandling result = error_func (error);
  switch (result.result)
    {
    case ErrorHandling::SUCCESS:
      return success_func (std::move (value));
    case ErrorHandling::FAILURE:
      throw_tox_exception (env, tox_traits<Subsystem>::module, method, result.error);
      break;
    case ErrorHandling::UNHANDLED:
      throw_illegal_state_exception (env, error, "Unknown error code");
      break;
    }

  return tox_success_t<SuccessFunc, ToxFunc, Args...> ();
}


template<typename ErrorFunc, typename SuccessFunc, typename ToxFunc, typename ...Args>
tox_success_t<SuccessFunc, ToxFunc, typename tox_fun_traits<ToxFunc>::subsystem_type *, Args...>
with_instance (JNIEnv *env,
               jint instanceNumber,
               char const *method,
               ErrorFunc error_func,
               SuccessFunc success_func,
               ToxFunc tox_func,
               Args ...args)
{
  typedef typename tox_fun_traits<ToxFunc>::subsystem_type subsystem_type;
  typedef typename tox_traits<subsystem_type>::events      events_type;

  return with_instance (env, instanceNumber,
    [=] (subsystem_type *tox, events_type &events)
      {
        (void)events;
        return with_error_handling<subsystem_type> (env, method, error_func, success_func, tox_func, tox, args...);
      }
  );
}


template<typename ErrorFunc, typename ToxFunc, typename ...Args>
tox_success_t<ignore, ToxFunc, typename tox_fun_traits<ToxFunc>::subsystem_type *, Args...>
with_instance (JNIEnv *env,
               jint instanceNumber,
               char const *method,
               ErrorFunc error_func,
               ToxFunc tox_func,
               Args ...args)
{
  return with_instance (env, instanceNumber, method, error_func, ignore (), tox_func, args...);
}


#ifdef HAVE_TOXAV
template<>
struct tox_traits<ToxAV>
{
private:
  struct deleter
  {
    void operator () (ToxAV *toxav)
    {
      toxav_kill (toxav);
    }
  };

public:
  typedef im::tox::tox4j::av::proto::AvEvents events;

  typedef std::unique_ptr<ToxAV, deleter> pointer;

  static char const *const module;
};
#endif


template<>
struct tox_traits<Tox>
{
private:
  struct deleter
  {
    void operator () (Tox *tox)
    {
      tox_kill (tox);
    }
  };

public:
  typedef im::tox::tox4j::core::proto::CoreEvents events;

  typedef std::unique_ptr<Tox, deleter> pointer;

  static char const *const module;
};
