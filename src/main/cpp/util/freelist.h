#pragma once

#include "util/synchronised.h"

#include <algorithm>


/**
 * Objects can conceptually have three states:
 * - LIVE: An instance is alive and running (value != nullptr).
 * - DEAD: The object has been destroyed (value = nullptr).
 * - COLLECTED: state is DEAD, and the object's instance number is in freelist_,
 *              meaning no references to it exist from either C++ or Java.
 */
template<typename T>
class freelist
{
public:
  typedef std::unique_ptr<T>	pointer;

private:
  synchronised_vector<pointer>	instances_;
  std::vector<std::size_t>	freelist_;

  bool
  is_free (std::size_t index) const
  {
    return std::find (freelist_.begin (), freelist_.end (), index) != freelist_.end ();
  }

  bool
  is_live (std::size_t index) const
  {
    return instances_.access (index,
      [] (pointer const &instance)
      { return instance != nullptr; }
    );
  }

public:
  constexpr explicit operator bool () const { return true; }

  std::size_t
  add (pointer instance)
  {
    // If there are free objects we can reuse
    if (!freelist_.empty ())
      {
        // use the last object that became unreachable (it will most likely be in cache)
        std::size_t index = freelist_.back ();
        freelist_.pop_back ();

        // Get a reference to the location in the instance list.
        instances_.access (index,
          [instance = std::move (instance)] (pointer &target) mutable
          {
            // It must be dead (and COLLECTED, because it's on the free list).
            assert (target == nullptr);
            // Reassign the instance.
            target = std::move (instance);
          }
        );

        return index;
      }

    // No free objects, so create a new one.
    return instances_.add (std::move (instance));
  }


  bool
  kill (std::size_t index)
  {
    return instances_.destroy (index);
  }


  bool
  finalize (std::size_t index)
  {
    if (is_live (index))
      {
        // This instance was leaked, kill it before setting it free.
        fprintf (stderr, "Leaked instance #%zu\n", index);
        bool destroyed = instances_.destroy (index);
        assert (destroyed);
        // If it's live, it cannot be free.
        assert (!is_free (index));
      }
    // An instance should never be on this list twice.
    else if (is_free (index))
      return false;

    assert (!is_live (index));

    freelist_.push_back (index);
    return true;
  }
};



template<typename T>
class synchronised_freelist
  : synchronised<freelist<T>>
{
  typedef typename freelist<T>::pointer pointer;

public:
  std::size_t
  add (pointer instance)
  {
    return this->access (
      [instance = std::move (instance)] (freelist<T> &self) mutable
      { return self.add (std::move (instance)); }
    );
  }

  bool
  kill (std::size_t index)
  {
    return this->access (
      [index] (freelist<T> &self)
      { return self.kill (index); }
    );
  }

  bool
  finalize (std::size_t index)
  {
    return this->access (
      [index] (freelist<T> &self)
      { return self.finalize (index); }
    );
  }
};
