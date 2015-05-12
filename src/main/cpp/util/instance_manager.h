#pragma once

#include <algorithm>
#include <cassert>
#include <deque>
#include <memory>
#include <mutex>
#include <vector>

#include "util/exceptions.h"
#include "util/to_string.h"


template<typename Pointer, typename Events>
class instance_manager
{
  std::vector<Pointer>                    instance_ptrs;
  std::vector<std::unique_ptr<Events>>    instance_events;
  std::deque<std::mutex>                  instance_locks;

  std::vector<jint> freelist;
  std::mutex mutex;


  bool
  check_instance_number (JNIEnv *env, jint instanceNumber, bool allow_zero)
  {
    if (instanceNumber < 0)
      {
        throw_illegal_state_exception (env, instanceNumber, "instance number out of range");
        return false;
      }

    // This can happen when an exception is thrown from the constructor, giving this object
    // an invalid state, containing instanceNumber = 0.
    if (!allow_zero && instanceNumber == 0)
      {
        throw_illegal_state_exception (env, instanceNumber, "function called on null instance");
        return false;
      }

    if (static_cast<std::size_t> (instanceNumber) > instance_ptrs.size ())
      {
        throw_illegal_state_exception (env, instanceNumber, "function called on invalid instance");
        return false;
      }

    return true;
  }


public:
  typedef typename Pointer::element_type Subsystem;

  instance_manager () = default;

  // Non-copyable.
  instance_manager (instance_manager const &) = delete;
  instance_manager &operator = (instance_manager const &) = delete;


  jint
  add (JNIEnv *env, Pointer instance, std::unique_ptr<Events> events)
  {
    std::lock_guard<std::mutex> lock (mutex);

    tox4j_assert (instance);
    tox4j_assert (events);

    // If there are free objects we can reuse..
    if (!freelist.empty ())
      {
        // ..use the last object that became unreachable (it will most likely be in cache).
        jint instanceNumber = freelist.back ();
        freelist.pop_back ();    // Remove it from the free list.

        tox4j_assert (!instance_ptrs[instanceNumber - 1]);
        tox4j_assert (!instance_events[instanceNumber - 1]);

        instance_ptrs[instanceNumber - 1] = std::move (instance);
        instance_events[instanceNumber - 1] = std::move (events);

        return instanceNumber;
      }

    // Otherwise, add a new one.
    instance_ptrs.push_back (std::move (instance));
    instance_events.push_back (std::move (events));
    instance_locks.emplace_back ();

    tox4j_assert (instance_ptrs.size () == instance_events.size ());
    tox4j_assert (instance_ptrs.size () == instance_locks.size ());
    return instance_ptrs.size ();
  }


  void
  kill (JNIEnv *env, jint instanceNumber)
  {
    std::lock_guard<std::mutex> lock (mutex);

    if (!check_instance_number (env, instanceNumber, false))
      return;

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

    if (!check_instance_number (env, instanceNumber, true))
      return;

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

    if (!check_instance_number (env, instanceNumber, false))
      return return_type ();

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
