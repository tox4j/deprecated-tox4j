#pragma once

#ifdef HAVE_TOXAV
#include <tox/av.h>
#endif
#include <tox/core.h>

#include <algorithm>
#include <vector>
#include <deque>
#include <functional>
#include <memory>
#include <mutex>
#include <sstream>


/*****************************************************************************
 * Identity function.
 */

static auto const identity = [](auto v) { return v; };


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


#define CAT(a, b) CAT_(a, b)
#define CAT_(a, b) a##b


template<typename T>
std::string
to_string (T const &v)
{
  std::ostringstream out;

  out << v;
  return out.str ();
}


template<typename ErrorT>
ErrorHandling
handle_error_enum (ErrorT error);


#define HANDLE(METHOD)                                            \
template<>                                                        \
ErrorHandling                                                     \
handle_error_enum<ERROR_CODE (METHOD)> (ERROR_CODE (METHOD) error)


template<typename Pointer, typename Events>
class instance_manager_base
{
  std::vector<Pointer>                    instance_ptrs;
  std::vector<std::unique_ptr<Events>>    instance_events;
  std::deque<std::mutex>                  instance_locks;

  std::vector<jint> freelist;
  std::mutex mutex;


public:
  typedef typename Pointer::element_type Subsystem;

  instance_manager_base () = default;

  // Non-copyable.
  instance_manager_base (instance_manager_base const &) = delete;
  instance_manager_base &operator = (instance_manager_base const &) = delete;


  jint
  add (Pointer instance, std::unique_ptr<Events> events)
  {
    std::lock_guard<std::mutex> lock (mutex);

    assert (instance);
    assert (events);

    // If there are free objects we can reuse..
    if (!freelist.empty ())
      {
        // ..use the last object that became unreachable (it will most likely be in cache).
        jint instanceNumber = freelist.back ();
        freelist.pop_back ();    // Remove it from the free list.

        assert (!instance_ptrs[instanceNumber - 1]);
        assert (!instance_events[instanceNumber - 1]);

        instance_ptrs[instanceNumber - 1] = std::move (instance);
        instance_events[instanceNumber - 1] = std::move (events);

        return instanceNumber;
      }

    // Otherwise, add a new one.
    instance_ptrs.push_back (std::move (instance));
    instance_events.push_back (std::move (events));
    instance_locks.emplace_back ();

    assert (instance_ptrs.size () == instance_events.size ());
    assert (instance_ptrs.size () == instance_locks.size ());
    return instance_ptrs.size ();
  }


  void
  kill (JNIEnv *env, jint instanceNumber)
  {
    std::lock_guard<std::mutex> lock (mutex);

    if (instanceNumber < 0)
      {
        throw_illegal_state_exception (env, instanceNumber, "instance number out of range");
        return;
      }

    if (instanceNumber == 0)
      {
        throw_illegal_state_exception (env, instanceNumber, "close called on null instance");
        return;
      }

    if (instanceNumber > (jint)instance_ptrs.size ())
      {
        throw_illegal_state_exception (env, instanceNumber, "close called on invalid instance");
        return;
      }

    // Lock before moving the pointers out.
    std::lock_guard<std::mutex> instance_lock (instance_locks[instanceNumber - 1]);

    // The destructors of these two are called inside the critical section entered above.
    Pointer dying_ptr = std::move (instance_ptrs[instanceNumber - 1]);
    std::unique_ptr<Events> dying_events = std::move (instance_events[instanceNumber - 1]);
  }


  void
  finalize (JNIEnv *env, jint instanceNumber)
  {
    std::lock_guard<std::mutex> lock (mutex);

    if (instanceNumber < 0)
      {
        throw_illegal_state_exception (env, instanceNumber, "instance number out of range");
        return;
      }

    if (instanceNumber == 0)
      // This can happen when an exception is thrown from the constructor, giving this object
      // an invalid state, containing instanceNumber = 0.
      return;

    if (instanceNumber > (jint)instance_ptrs.size ())
      {
        throw_illegal_state_exception (env, instanceNumber, "finalize called on invalid instance");
        return;
      }

    // An instance should never be on this list twice.
    if (std::find (freelist.begin (), freelist.end (), instanceNumber) != freelist.end ())
      {
        throw_illegal_state_exception (env, instanceNumber, "instance already on free list");
        return;
      }

    // The C++ side should already have been killed.
    if (instance_ptrs[instanceNumber - 1])
      {
        throw_illegal_state_exception (env, instanceNumber, "Leaked Tox instance #" + to_string (instanceNumber));
        return;
      }

    freelist.push_back (instanceNumber);
  }


  template<typename Func>
  auto
  with_instance (JNIEnv *env, jint instanceNumber, Func func)
  {
    typedef typename std::result_of<Func (Subsystem *, Events &)>::type return_type;

    std::lock_guard<std::mutex> lock (mutex);

    if (instanceNumber < 0)
      {
        throw_illegal_state_exception (env, instanceNumber, "instance number out of range");
        return return_type ();
      }

    if (instanceNumber == 0)
      {
        throw_illegal_state_exception (env, instanceNumber, "function called on incomplete object");
        return return_type ();
      }

    if (instanceNumber > (jint)instance_ptrs.size ())
      {
        throw_illegal_state_exception (env, instanceNumber, "function called on invalid instance");
        return return_type ();
      }

    std::lock_guard<std::mutex> instance_lock (instance_locks[instanceNumber - 1]);

    Pointer &ptr = instance_ptrs.at (instanceNumber - 1);
    Events &events = *instance_events.at (instanceNumber - 1);

    if (!ptr)
      {
        throw_tox_killed_exception (env, instanceNumber, "function invoked on killed instance");
        return return_type ();
      }

    return func (ptr.get (), events);
  }
};


template<typename Subsystem>
extern char const *const module_name;


template<typename Pointer, typename Events>
struct instance_manager
  : instance_manager_base<Pointer, Events>
{
  typedef typename instance_manager_base<Pointer, Events>::Subsystem Subsystem;

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
        throw_tox_exception (env, module_name<Subsystem>, method, result.error);
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
      [=] (Subsystem *tox, Events &events)
        {
          (void)events;
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
      [&] (Subsystem *tox, Events &events)
        {
          (void)events;
          return tox_func (tox, args...);
        }
    );
  }
};



#define ERROR_CODE(METHOD)                          \
  CAT (SUBSYSTEM, _ERR_##METHOD)

#define success_case(METHOD)                        \
  case CAT (ERROR_CODE (METHOD), _OK):              \
    return success()

#define failure_case(METHOD, ERROR)                 \
  case CAT (ERROR_CODE (METHOD), _##ERROR):         \
    return failure (#ERROR)
