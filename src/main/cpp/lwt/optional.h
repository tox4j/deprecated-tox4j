#pragma once

#include <cassert>

#include <utility>


namespace lwt
{
  struct nullopt_t
  { };


  template<typename T>
  struct optional
  {
    optional &operator = (nullopt_t)
    {
      destroy ();
      return *this;
    }

    explicit operator bool () const { return initialised_; }

    T const *operator -> () const
    {
      assert (initialised_);
      return &value ();
    }

    T *operator -> ()
    {
      assert (initialised_);
      return &value ();
    }

    template<typename ...Args>
    void emplace (Args &&...args)
    {
      destroy ();
      new (static_cast<void *> (data_)) T (std::forward<Args> (args)...);
      initialised_ = true;
    }

  private:
    void destroy ()
    {
      if (initialised_)
        {
          value ().~T ();
          initialised_ = false;
        }
    }

    T       &value ()       { assert (initialised_); return *static_cast<T       *> (static_cast<void       *> (data_)); }
    T const &value () const { assert (initialised_); return *static_cast<T const *> (static_cast<void const *> (data_)); }

    alignas (T) unsigned char data_[sizeof (T)];
    bool initialised_;
  };
}
