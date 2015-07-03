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
  struct instance_pointers
  {
    ObjectP object_p;
    EventsP events_p;

    explicit operator bool () const
    {
      // These come and go hand in hand.
      assert (!object_p == !events_p);
      return object_p != nullptr;
    }


    /**
     * Contains the actual pointer/reference to the instance_pointers data.
     * This is used so we can unlock the instance_manager after getting the
     * instance and before calling the function in with_instance, which may take
     * a long time and would unnecessarily block the manager during its execution.
     */
    struct locked
    {
      Object *object;
      Events *events;
      std::unique_lock<std::mutex> lock;

      explicit operator bool () const
      {
        // These come and go hand in hand.
        assert (!object == !events);
        return object != nullptr;
      }
    };

    /**
     * Copies the pointers together with their lock into a single object without
     * pointers or references into the instance_pointers object, which may be
     * moved when its containing vector reallocates.
     */
    locked
    get (std::unique_lock<std::mutex> lock) const
    {
      return {
        object_p.get (),
        events_p.get (),
        std::move (lock),
      };
    }
  };

  // The instances and locks lists are always equal in size (class invariant).
  std::vector<instance_pointers> instances;
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

    // An instance should never be on this list twice.
    if (std::find (freelist.begin (), freelist.end (), instanceNumber) != freelist.end ())
      {
        throw_illegal_state_exception (env, instanceNumber, "instance already on free list");
        return false;
      }

    return true;
  }


public:
  instance_manager () = default;

  // Non-copyable.
  instance_manager (instance_manager const &) = delete;
  instance_manager &operator = (instance_manager const &) = delete;


  /**
   * Hands over management of the object and events pointers to the manager. The
   * returned instance number can be used to call with_instance for access and
   * kill/finalize for destruction/cleanup.
   */
  jint
  add (JNIEnv *env, ObjectP object_p, EventsP events_p)
  {
    // The manager is locked for the entire duration of this function. No access
    // or mutation may happen during an add, because we might reallocate and
    // move objects here.
    std::lock_guard<std::mutex> lock (mutex);

    tox4j_assert (object_p);
    tox4j_assert (events_p);

    instance_pointers instance_p = {
      std::move (object_p),
      std::move (events_p),
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

        instances[instanceNumber - 1] = std::move (instance_p);

        return instanceNumber;
      }

    // Otherwise, add a new one.
    instances.push_back (std::move (instance_p));
    locks.emplace_back ();

    tox4j_assert (instances.size () == locks.size ());
    return instances.size ();
  }


  /**
   * Destroys an instance, deallocating both the object and the events.
   *
   * This function is idempotent as long as there was no finalize call with the
   * passed instance number in between.
   */
  void
  kill (JNIEnv *env, jint instanceNumber)
  {
    // Lock the manager, since we're going to access the lists while add() might
    // be modifying them.
    std::lock_guard<std::mutex> lock (mutex);

    if (!check_instance_number (env, instanceNumber, true))
      return;

    // Lock before moving the pointers out.
    std::lock_guard<std::mutex> instance_lock (locks[instanceNumber - 1]);

    // The instance destructor is called inside the critical section entered above.
    auto dying = std::move (instances[instanceNumber - 1]);
  }


  /**
   * Add the instance number to the freelist, making it a candidate for reuse.
   *
   * The kill function *must* be called before calling this function (otherwise,
   * undefined behaviour).
   * This function is *not* idempotent. It will throw a Java exception if it's
   * called twice on the same instance number (given there was no add in between
   * that returned that instance number).
   */
  void
  finalize (JNIEnv *env, jint instanceNumber)
  {
    // Lock the manager, since we're going to access the lists while add() might
    // be modifying them.
    std::lock_guard<std::mutex> lock (mutex);

    // Don't throw on null instances, but also don't put it on the freelist.
    if (!check_instance_number (env, instanceNumber, true))
      return;

    // The C++ side should already have been killed.
    if (instances[instanceNumber - 1])
      {
        throw_illegal_state_exception (env, instanceNumber, "Leaked Tox instance #" + to_string (instanceNumber));
        return;
      }

    tox4j_assert (instanceNumber != 0);
    freelist.push_back (instanceNumber);
  }


  /**
   * Access the instance identified by the instance number. Func must be a
   * callable type and accept an Object* and an Events&. The result of the
   * function call is returned by with_instance.
   *
   * For the duration of the call to Func, the instance will be locked, and the
   * manager will be unlocked. This means that multiple concurrent calls to a
   * single instance are not possible, but multiple concurrent calls to different
   * instances is permitted.
   */
  template<typename Func>
  auto
  with_instance (JNIEnv *env, jint instanceNumber, Func func)
  {
    typedef typename std::result_of<Func (Object *, Events &)>::type return_type;

    // After this object is initialised, it is independent of the pointers'
    // memory location in the manager.
    auto instance = [this, env, instanceNumber]
      {
        // Lock the instance manager only while accessing the list.
        std::lock_guard<std::mutex> lock (mutex);

        if (!check_instance_number (env, instanceNumber, false))
          return typename instance_pointers::locked { };

        // Lock the instance, get the pointers out of the instance vector, and
        // unlock the manager (by lock_guard's destructor).
        return instances[instanceNumber - 1].get (std::unique_lock<std::mutex> (locks[instanceNumber - 1]));
      } ();

    if (!instance)
      return return_type ();

    // After this function returns, the unique_lock in the instance object will
    // be destroyed, unlocking the instance.
    return func (instance.object, *instance.events);
  }
};
