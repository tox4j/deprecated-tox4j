#pragma once

#include <cassert>
#include <memory>
#include <mutex>
#include <vector>


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


template<typename T>
class synchronised_vector
{
  std::vector<T>				value_;
  std::vector<std::unique_ptr<std::mutex>>	mutex_;

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

  // The instance is moved into this function, then moved into the vector.
  // This function returns the index, which can be used to access or destroy
  // the instance.
  std::size_t
  add (T instance)
  {
    value_.emplace_back (std::move (instance));
    mutex_.emplace_back (new std::mutex);
    return size () - 1;
  }

  /**
   * Returns true iff an object was destroyed. Calling destroy twice on the same
   * index will return false the second time.
   */
  bool
  destroy (std::size_t index)
  {
    std::lock_guard<std::mutex> lock (*mutex_.at (index));
    // The instance is moved out of the vector into the local variable, which
    // ends its lifetime just before the lock_guard ends, guaranteeing that the
    // destructor is called inside the instance's critical section.
    T dying = std::move (value_.at (index));
    return static_cast<bool> (dying);
  }

  template<typename Func>
  auto
  access (std::size_t index, Func func)
  { return synchronised_access<T &> (*mutex_.at (index), value_.at (index), std::move (func)); }

  template<typename Func>
  auto
  access (std::size_t index, Func func) const
  { return synchronised_access<T const &> (*mutex_.at (index), value_.at (index), std::move (func)); }
};
