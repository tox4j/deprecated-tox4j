#pragma once

#include <cassert>
#include <deque>
#include <memory>
#include <mutex>
#include <vector>


/**
 * Helper function to support calling a function with either a T& or a T const&.
 *
 * The value needs to be contextually convertible to bool. If after conversion
 * it evaluates to true, the passed mutex is locked before calling the function
 * and unlocked after it returns. If it evaluates to false, the assumption is
 * that the object is empty/null and doesn't need locking.
 */
template<typename T, typename Func>
static auto
synchronised_access (std::mutex &mutex, T value, Func func)
{
  if (value)
    {
      // We only need to lock if there is actually a live object here.
      std::lock_guard<std::mutex> lock (mutex);
      return func (value);
    }
  // Otherwise, this is a null object that doesn't need to be locked on.
  return func (value);
}


/**
 * This class ensures that any access to the contained value is synchronised by
 * a companion mutex.
 *
 * Construction and destruction of the contained object are not in a protected
 * critical section, so client code must ensure that the object is not accessed
 * and destroyed at the same time.
 */
template<typename T>
class synchronised
{
  std::mutex mutex_;
  T value_;

public:
  template<typename Func>
  auto
  access (Func func)
  { return synchronised_access<T &> (mutex_, value_, std::move (func)); }

  template<typename Func>
  auto
  access (Func func) const
  { return synchronised_access<T const &> (mutex_, value_, std::move (func)); }
};


/**
 * Essentially a vector<synchronised>, but decouples the mutex from the object.
 */
template<typename T>
class synchronised_vector
{
  std::vector<T> value_;
  mutable std::deque<std::mutex> mutex_;

public:
  bool
  empty () const
  {
    assert (value_.empty () == mutex_.empty ());
    return value_.empty ();
  }

  std::size_t
  size () const
  {
    assert (value_.size () == mutex_.size ());
    return value_.size ();
  }

  /**
   * The instance is moved into this function, then moved into the vector.
   * This function returns the index, which can be used to access or destroy
   * the instance or to replace it with another instance.
   */
  std::size_t
  add (T instance)
  {
    value_.emplace_back (std::move (instance));
    mutex_.emplace_back ();
    return size () - 1;
  }

  bool
  replace (std::size_t index, T instance)
  {
    bool replaced = static_cast<bool> (value_.at (index));
    value_.at (index) = std::move (instance);
    return replaced;
  }

  /**
   * Returns true iff an object was destroyed. Calling destroy twice on the same
   * index will return false the second time.
   */
  bool
  destroy (std::size_t index)
  {
    std::lock_guard<std::mutex> lock (mutex_.at (index));
    // The instance is moved out of the vector into the local variable, which
    // ends its lifetime just before the lock_guard ends, guaranteeing that the
    // destructor is called inside the instance's critical section.
    T dying = std::move (value_.at (index));
    return static_cast<bool> (dying);
  }

  template<typename Func>
  auto
  access (std::size_t index, Func func)
  { return synchronised_access<T &> (mutex_.at (index), value_.at (index), std::move (func)); }

  template<typename Func>
  auto
  access (std::size_t index, Func func) const
  { return synchronised_access<T const &> (mutex_.at (index), value_.at (index), std::move (func)); }
};
