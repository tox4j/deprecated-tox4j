#pragma once

#include <utility>

template<typename T>
struct wrapped_value
{
  T value;
  T unwrap () { return std::move (value); }

  template<typename FuncT, typename ...Args>
  static wrapped_value
  wrap (FuncT func, Args ...args)
  {
    return { func (args...) };
  }
};


template<>
struct wrapped_value<void>
{
  void unwrap () const { }

  template<typename FuncT, typename ...Args>
  static wrapped_value
  wrap (FuncT func, Args ...args)
  {
    func (args...);
    return { };
  }
};


template<typename FuncT, typename ...Args>
wrapped_value<typename std::result_of<FuncT (Args...)>::type>
wrap_void (FuncT func, Args ...args)
{
  return wrapped_value<typename std::result_of<FuncT (Args...)>::type>::wrap (func, args...);
}
