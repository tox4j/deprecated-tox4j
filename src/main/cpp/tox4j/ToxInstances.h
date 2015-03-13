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
#include <memory>
#include <mutex>
#include <functional>


struct scope_guard
{
  scope_guard (std::function<void ()> enter)
    : exit ([=] { enter (); printf (" [done]"); fflush (stdout); })
  { enter (); printf ("\n"); }

  scope_guard (std::function<void ()> enter, std::function<void ()> exit)
    : exit (exit)
  { enter (); printf ("\n"); fflush (stdout); }

  ~scope_guard ()
  { this->exit (); printf ("\n"); fflush (stdout); }

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


template<typename ToxTraits>
class instance_manager;

template<typename ToxTraits>
class tox_instance
{
  friend class instance_manager<ToxTraits>;

  // Objects of this class can conceptually have three states:
  // - LIVE: A tox instance is alive and running.
  // - DEAD: The object is empty, all pointers are nullptr.
  // - COLLECTED: state is DEAD, and the object's instance_number is in instance_freelist.
  bool live = true;

  typedef typename ToxTraits::subsystem subsystem_type;
  typedef typename ToxTraits::events events_type;
  typedef typename ToxTraits::deleter deleter_type;

public:
  typedef std::unique_ptr<subsystem_type, deleter_type> pointer;

  pointer tox;
  std::unique_ptr<events_type> events;

private:
  std::unique_ptr<std::mutex> mutex;

public:
  bool isLive () const { return  live; }
  bool isDead () const { return !live; }


  template<typename Func, typename ... Args>
  typename std::result_of<Func (typename pointer::pointer, events_type &, Args ...)>::type
  with_lock (Func func, Args const &...args) const
  {
    assert (mutex != nullptr);
    std::lock_guard<std::mutex> lock (*mutex);
#if 0
    scope_guard {
      [&]{ printf ("locking instance %zd", ID (tox)); },
      [&]{ printf ("unlocking instance %zd", ID (tox)); },
    };
#endif
    assert (events != nullptr);
    return func (tox.get (), *events, args ...);
  }


  void assertValid () const
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


  tox_instance (pointer &&tox,
                std::unique_ptr<events_type> &&events,
                std::unique_ptr<std::mutex> &&mutex)
    : tox (std::move (tox))
    , events (std::move (events))
    , mutex (std::move (mutex))
  {
#if 0
    scope_guard {
      [&]{ printf ("new %zd", ID (tox)); }
    };
#endif
    assertValid ();
  }


  // Move members from another object into this new one, then set the old one to the DEAD state.
  tox_instance (tox_instance && rhs)
    : live (rhs.live)
    , tox (std::move (rhs.tox))
    , events (std::move (rhs.events))
    , mutex (std::move (rhs.mutex))
  {
#if 0
    scope_guard {
      [&]{ printf ("move: %zd -> %zd", ID (rhs.tox), ID (tox)); },
    };
#endif
    rhs.live = false;

    this->assertValid ();
    rhs.assertValid ();
  }


  // Move members from another object into this existing one, then set the right hand side to the
  // DEAD state. This object is then live again.
  tox_instance &operator = (tox_instance &&rhs)
  {
#if 0
    scope_guard {
      [&] { printf ("assign: %zd <- %zd", ID (tox), ID (rhs.tox)); }
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


template<typename ToxTraits>
class instance_manager
{
  typedef tox_instance<ToxTraits> instance_type;

  // This struct should remain small. Check some assumptions here.
  static_assert (sizeof (instance_type) == sizeof (void *) * 4,
                 "tox_instance has unexpected members or padding");

  std::vector<instance_type> instances;
  std::vector<jint> freelist;
  std::mutex mutex;

  bool is_locked () const
  {
    return !const_cast<std::mutex &> (mutex).try_lock ();
  }

public:
  std::unique_lock<std::mutex> lock ()
  {
    return std::unique_lock<std::mutex> (mutex);
  }

  bool isValid (jint instance_number) const
  {
    assert (is_locked ());
    return instance_number > 0
        && (size_t)instance_number <= instances.size ();
  }

  bool isFree (jint instance_number) const
  {
    assert (is_locked ());
    return std::find (freelist.begin (), freelist.end (), instance_number) != freelist.end ();
  }

  void setFree (jint instance_number)
  {
    assert (is_locked ());
    freelist.push_back (instance_number);
  }

  instance_type const &operator [] (jint instance_number) const
  {
    assert (is_locked ());
    return instances[instance_number - 1];
  }


private:
  instance_type remove (jint instance_number)
  {
    assert (is_locked ());
    return std::move (instances[instance_number - 1]);
  }

  bool empty () const
  {
    assert (is_locked ());
    return instances.empty ();
  }

  size_t size () const
  {
    assert (is_locked ());
    return instances.size ();
  }


public:
  jint add (instance_type && instance)
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
    return (jint)instances.size ();
  }


  void kill (JNIEnv *env, jint instanceNumber)
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

    assert (dying.isLive ());
    dying.assertValid ();
    std::lock_guard<std::mutex> ilock (*dying.mutex);
  }


  void finalize (JNIEnv *env, jint instanceNumber)
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
        instance_type dying (remove (instanceNumber));
        assert (dying.isLive ());
        dying.assertValid ();
        std::lock_guard<std::mutex> ilock (*dying.mutex);
      }

    assert ((*this)[instanceNumber].isDead ());
    setFree (instanceNumber);
  }


  static instance_manager self;
};

template<typename ToxTraits>
instance_manager<ToxTraits> instance_manager<ToxTraits>::self;
