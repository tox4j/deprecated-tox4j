#pragma once

#ifdef HAVE_TOXAV
#include <tox/av.h>
#endif
#include <tox/core.h>

#ifdef HAVE_TOXAV
#include "im_tox_tox4j_ToxAvImpl.h"
#endif
#include "im_tox_tox4j_ToxCoreImpl.h"

#ifdef HAVE_TOXAV
#include "Av.pb.h"
#endif
#include "Core.pb.h"

#include <algorithm>
#include <functional>
#include <memory>
#include <mutex>


#define DEBUG_TOX_INSTANCE 0


struct scope_guard
{
  scope_guard (std::function<void ()> enter)
    : exit ([=] { enter (); fprintf (stderr, " [done]"); })
  { enter (); fprintf (stderr, "\n"); }

  scope_guard (std::function<void ()> enter, std::function<void ()> exit)
    : exit (exit)
  { enter (); fprintf (stderr, "\n"); }

  ~scope_guard ()
  { this->exit (); fprintf (stderr, "\n"); }

private:
  std::function<void ()> enter;
  std::function<void ()> exit;
};

#define CAT(a, b) CAT_(a, b)
#define CAT_(a, b) a##b
#define scope_guard scope_guard CAT(scope_guard_, __LINE__)


template<typename T>
std::string
to_string (T const &v)
{
  std::ostringstream out;

  out << v;
  return out.str ();
}


template<typename T>
struct unique_id
{
  static std::size_t
  get (T *p)
  {
    if (p == nullptr)
      return 0;

    std::lock_guard<std::mutex> lock (mutex);
    auto found = ids.find (p);
    if (found == ids.end ())
      {
        ids.insert (std::make_pair (p, ids.size () + 1));
        return ids.size ();
      }
    return found->second;
  }

private:
  static std::mutex mutex;
  static std::map<T *, std::size_t> ids;
};

template<typename T> std::mutex unique_id<T>::mutex;
template<typename T> std::map<T *, std::size_t> unique_id<T>::ids;

template<typename T, typename Delete>
std::size_t
ID (std::unique_ptr<T, Delete> const &p)
{
  return unique_id<T>::get (p.get ());
}


template<typename Subsystem>
struct tox_traits;

template<typename Subsystem>
struct tox_traits<Subsystem const>
  : tox_traits<Subsystem>
{ };

template<typename Subsystem, typename Traits = tox_traits<Subsystem>>
class instance_manager;

template<typename Subsystem>
class instance_manager<Subsystem const>
  : public instance_manager<Subsystem>
{ };

template<typename Subsystem, typename Traits = tox_traits<Subsystem>>
class tox_instance final
{
  friend class instance_manager<Subsystem, Traits>;

  // Objects of this class can conceptually have three states:
  // - LIVE: A tox instance is alive and running.
  // - DEAD: The object is empty, all pointers are nullptr.
  // - COLLECTED: state is DEAD, and the object's instance_number is in instance_freelist.
  bool live = true;

  typedef typename Traits::events events_type;

public:
  typedef typename Traits::pointer pointer;

  pointer tox;
  std::unique_ptr<events_type> events;

private:
  std::unique_ptr<std::mutex> mutex;

  void
  assertValid () const
  {
    if (isLive ())
      {
        assert (tox != nullptr);
        assert (events != nullptr);
        assert (mutex != nullptr);
      }
    else
      {
        assert (tox == nullptr);
        assert (events == nullptr);
        assert (mutex == nullptr);
      }
  }


public:
  bool isLive () const { return  live; }
  bool isDead () const { return !live; }


  template<typename Func, typename ...Args>
  typename std::result_of<Func (typename pointer::pointer, events_type &, Args...)>::type
  with_lock (Func func, Args const &...args) const
  {
    assert (isLive ());
    assertValid ();

    std::lock_guard<std::mutex> lock (*mutex);
#if DEBUG_TOX_INSTANCE
    scope_guard {
      [&]{ fprintf (stderr, "locking instance %zd", ID (tox)); },
      [&]{ fprintf (stderr, "unlocking instance %zd", ID (tox)); },
    };
#endif
    return func (tox.get (), *events, args...);
  }


  tox_instance (pointer tox,
                std::unique_ptr<events_type> events,
                std::unique_ptr<std::mutex> mutex)
    : tox (std::move (tox))
    , events (std::move (events))
    , mutex (std::move (mutex))
  {
#if DEBUG_TOX_INSTANCE
    scope_guard {
      [&]{ fprintf (stderr, "new %zd", ID (tox)); }
    };
#endif
    assertValid ();
  }


  // Move members from another object into this new one, then set the old one to the DEAD state.
  tox_instance (tox_instance &&rhs)
    : live (rhs.live)
    , tox (std::move (rhs.tox))
    , events (std::move (rhs.events))
    , mutex (std::move (rhs.mutex))
  {
#if DEBUG_TOX_INSTANCE
    scope_guard {
      [&]{ fprintf (stderr, "move: %zd -> %zd", ID (rhs.tox), ID (tox)); },
    };
#endif
    rhs.live = false;

    this->assertValid ();
    rhs.assertValid ();
  }


  ~tox_instance ()
  {
    if (isLive ())
      // The caller must get the mutex out and lock it before destroying the instance.
      assert (mutex == nullptr);
    else
      assertValid ();
  }


  // Move members from another object into this existing one, then set the right hand side to the
  // DEAD state. This object is then live again.
  tox_instance &
  operator = (tox_instance &&rhs)
  {
#if DEBUG_TOX_INSTANCE
    scope_guard {
      [&] { fprintf (stderr, "assign: %zd <- %zd", ID (tox), ID (rhs.tox)); }
    };
#endif
    assert (this->isDead ());
    assert (rhs.isLive ());
    this->assertValid ();
    rhs.assertValid ();

    tox = std::move (rhs.tox);
    events = std::move (rhs.events);
    mutex = std::move (rhs.mutex);
    rhs.live = false;
    this->live = true;

    this->assertValid ();
    rhs.assertValid ();

    return *this;
  }
};


template<typename Subsystem, typename Traits>
class instance_manager
{
  typedef tox_instance<Subsystem, Traits> instance_type;

  // This struct should remain small. Check some assumptions here.
  static_assert (sizeof (instance_type) == sizeof (void *) * 4,
      "tox_instance has unexpected members or padding");

  static_assert (
      std::is_move_constructible<instance_type>::value
      && std::is_move_assignable<instance_type>::value,
      "tox_instance is not moveable");
  static_assert (
      !std::is_copy_constructible<instance_type>::value
      && !std::is_copy_assignable<instance_type>::value,
      "tox_instance is copyable but should only be moveable");

  std::vector<instance_type> instances;
  std::vector<jint> freelist;
  std::mutex mutex;

  void
  check_locked () const
  {
    assert (!const_cast<std::mutex &> (mutex).try_lock ());
  }

  // Private constructor: this is a singleton.
  instance_manager () = default;

  // Non-copyable.
  instance_manager (instance_manager const &) = delete;
  instance_manager &operator = (instance_manager const &) = delete;

  ~instance_manager ()
  {
    // Explicitly kill the instances, because their mutex needs to be locked
    // before calling tox_kill.
    while (!instances.empty ())
      kill (std::move (instances.back ()));
  }

public:
  std::unique_lock<std::mutex>
  lock ()
  {
    return std::unique_lock<std::mutex> (mutex);
  }

  jint
  size () const
  {
    check_locked ();
    return instances.size ();
  }

  bool
  isValid (jint instance_number) const
  {
    check_locked ();
    return instance_number > 0
        && instance_number <= size ();
  }

  bool
  isFree (jint instance_number) const
  {
    check_locked ();
    return std::find (freelist.begin (), freelist.end (), instance_number) != freelist.end ();
  }

  instance_type const &
  operator [] (jint instance_number) const
  {
    check_locked ();
    assert (isValid (instance_number));
    return instances[instance_number - 1];
  }


private:
  void
  setFree (jint instance_number)
  {
    check_locked ();
    assert (!isFree (instance_number));
    freelist.push_back (instance_number);
  }

  instance_type
  remove (jint instance_number)
  {
    check_locked ();
    return std::move (instances[instance_number - 1]);
  }

  bool
  empty () const
  {
    check_locked ();
    return instances.empty ();
  }


  static void
  kill (instance_type dying)
  {
    dying.assertValid ();
    if (!dying.isLive ())
      return;
    auto mutex = std::move (dying.mutex);
    std::lock_guard<std::mutex> ilock (*mutex);
  }


public:
  jint
  add (instance_type instance)
  {
    std::lock_guard<std::mutex> lock (mutex);

    // If there are free objects we can reuse..
    if (!freelist.empty ())
      {
        // ..use the last object that became unreachable (it will most likely be in cache).
        jint instance_number = freelist.back ();
        assert (instance_number >= 1);
        freelist.pop_back ();    // Remove it from the free list.
        assert (instances[instance_number - 1].isDead ());
        instances[instance_number - 1] = std::move (instance);
        assert (instances[instance_number - 1].isLive ());
        return instance_number;
      }

    // Otherwise, add a new one.
    instances.push_back (std::move (instance));
    return size ();
  }


  void
  kill (JNIEnv *env, jint instanceNumber)
  {
    std::lock_guard<std::mutex> lock (mutex);

    if (instanceNumber < 0)
      {
        throw_illegal_state_exception (env, instanceNumber, "Tox instance out of range");
        return;
      }

    if (!isValid (instanceNumber))
      {
        throw_tox_killed_exception (env, instanceNumber, "close called on invalid instance");
        return;
      }

    // After this move, the pointers in instance_vector[instance_number] will all be nullptr...
    instance_type dying (remove (instanceNumber));

    // ... so that this check will fail, if the function is called twice on the same instance.
    if (!dying.tox)
      {
#if 0
        throw_tox_killed_exception (env, instanceNumber, "close called on already closed instance");
#endif
        return;
      }

    kill (std::move (dying));
  }


  void
  finalize (JNIEnv *env, jint instanceNumber)
  {
    if (instanceNumber == 0)
      // This can happen when an exception is thrown from the constructor, giving this object
      // an invalid state, containing instanceNumber = 0.
      return;

    std::lock_guard<std::mutex> lock (mutex);
    if (empty ())
      {
        throw_illegal_state_exception (env, instanceNumber, "Tox instance manager is empty");
        return;
      }

    if (!isValid (instanceNumber))
      {
        throw_illegal_state_exception (env, instanceNumber,
            "Tox instance out of range (max: " + to_string (size () - 1) + ")");
        return;
      }

    // An instance should never be on this list twice.
    if (isFree (instanceNumber))
      {
        throw_illegal_state_exception (env, instanceNumber, "Tox instance already on free list");
        return;
      }

    // This instance was leaked, kill it before setting it free.
    if ((*this)[instanceNumber].isLive ())
      {
        fprintf (stderr, "Leaked Tox instance #%d\n", instanceNumber);
        kill (remove (instanceNumber));
      }

    assert ((*this)[instanceNumber].isDead ());
    setFree (instanceNumber);
  }


  static instance_manager self;
};
