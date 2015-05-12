#pragma once

#include <algorithm>
#include <cassert>
#include <deque>
#include <memory>
#include <mutex>
#include <vector>

#include "util/exceptions.h"
#include "util/to_string.h"


/**
 * The base instance_manager class. Contains functions to add, destroy, and
 * finalise pairs of objects. It manages an Instance and an Events object
 * together, with
 *
 * The creation of an ObjectP and EventsP is the responsibility of the caller.
 * After handing them to the manager, the manager will take care of cleaning
 * them up. Instance numbers returned by add() are used to access and delete
 * instances. Instance numbers may be reused after finalize() is called on them.
 * The finalize() function may not be called before kill(). The kill() function
 */
template<typename ObjectP, typename EventsP>
class instance_manager
{
protected:
  typedef typename ObjectP::element_type Object;
  typedef typename EventsP::element_type Events;

private:
  /**
   * Holds an object and an events pointer. The choice of putting this into the
   * instance_manager instead of just managing a single object was that this way,
   * the client code will never need to see unique_ptrs, and can simply operate
   * on object pointers and events references.
   */
  struct instance_pair
  {
    ObjectP object;
    EventsP events;

    explicit operator bool () const
    {
      // These come and go hand in hand.
      assert (!object == !events);
      return static_cast<bool> (object);
    }
  };

  std::vector<instance_pair> instances;
  // Locks are managed separately so we can use trivial move semantics to destroy
  // and reassign instance_pairs. A deque is used so we don't need an additional
  // indirection that vector<unique_ptr<mutex>> would have.
  std::deque<std::mutex> locks;

  // Contains indices (+1) into the instances/locks lists of finalised objects.
  std::vector<jint> freelist;
  // The global lock for the instance manager.
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
    if (instanceNumber == 0)
      {
        if (!allow_zero)
          throw_illegal_state_exception (env, instanceNumber, "function called on null instance");
        // Null instances are OK, but should still not be processed.
        return false;
      }

    if (static_cast<std::size_t> (instanceNumber) > instances.size ())
      {
        throw_illegal_state_exception (env, instanceNumber, "function called on invalid instance");
        return false;
      }

    return true;
  }


public:
  instance_manager () = default;

  // Non-copyable.
  instance_manager (instance_manager const &) = delete;
  instance_manager &operator = (instance_manager const &) = delete;


  jint
  add (JNIEnv *env, ObjectP object, EventsP events)
  {
    std::lock_guard<std::mutex> lock (mutex);

    tox4j_assert (object);
    tox4j_assert (events);

    instance_pair instance = {
      std::move (object),
      std::move (events),
    };

    // If there are free objects we can reuse..
    if (!freelist.empty ())
      {
        // ..use the last object that became unreachable (it will most likely be in cache).
        jint instanceNumber = freelist.back ();
        freelist.pop_back ();    // Remove it from the free list.

        // The null instance should never be on the freelist.
        tox4j_assert (instanceNumber >= 1);
        // All instances on the freelist should be empty.
        tox4j_assert (!instances[instanceNumber - 1]);

        instances[instanceNumber - 1] = std::move (instance);

        return instanceNumber;
      }

    // Otherwise, add a new one.
    instances.push_back (std::move (instance));
    locks.emplace_back ();

    tox4j_assert (instances.size () == locks.size ());
    return instances.size ();
  }


  void
  kill (JNIEnv *env, jint instanceNumber)
  {
    std::lock_guard<std::mutex> lock (mutex);

    if (!check_instance_number (env, instanceNumber, false))
      return;

    // Lock before moving the pointers out.
    std::lock_guard<std::mutex> instance_lock (locks[instanceNumber - 1]);

    // The instance destructor is called inside the critical section entered above.
    auto dying = std::move (instances[instanceNumber - 1]);
  }


  void
  finalize (JNIEnv *env, jint instanceNumber)
  {
    std::lock_guard<std::mutex> lock (mutex);

    // Don't throw on null instances, but also don't put it on the freelist.
    if (!check_instance_number (env, instanceNumber, true))
      return;

    // An instance should never be on this list twice.
    if (std::find (freelist.begin (), freelist.end (), instanceNumber) != freelist.end ())
      {
        throw_illegal_state_exception (env, instanceNumber, "instance already on free list");
        return;
      }

    // The C++ side should already have been killed.
    if (instances[instanceNumber - 1])
      {
        throw_illegal_state_exception (env, instanceNumber, "Leaked Tox instance #" + to_string (instanceNumber));
        return;
      }

    tox4j_assert (instanceNumber != 0);
    freelist.push_back (instanceNumber);
  }


  template<typename Func>
  auto
  with_instance (JNIEnv *env, jint instanceNumber, Func func)
  {
    typedef typename std::result_of<Func (Object *, Events &)>::type return_type;

    std::lock_guard<std::mutex> lock (mutex);

    if (!check_instance_number (env, instanceNumber, false))
      return return_type ();

    std::lock_guard<std::mutex> instance_lock (locks[instanceNumber - 1]);

    instance_pair &instance = instances.at (instanceNumber - 1);

    if (!instance)
      {
        throw_tox_killed_exception (env, instanceNumber, "function invoked on killed instance");
        return return_type ();
      }

    return func (instance.object.get (), *instance.events);
  }
};
