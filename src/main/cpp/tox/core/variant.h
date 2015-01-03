#pragma once

#include <cassert>
#include <cstddef>
#include <type_traits>


template<typename T>
struct variant_member
{
  std::size_t tag;
  T value;
};

template<std::size_t Index, typename ...Types>
union variant_storage;

template<std::size_t Index, typename Head, typename ...Tail>
union variant_storage<Index, Head, Tail...>
{
  typedef variant_storage<Index + 1, Tail...> tail_type;

  variant_member<Head> head;
  tail_type tail;

  template<typename T>
  variant_storage (T const &value)
    : tail (value)
  { }

  variant_storage (Head const &head)
    : head { Index, head }
  { }

  ~variant_storage ()
  {
    if (head.tag == Index)
      head.value.~Head ();
    else
      tail.~tail_type ();
  }

  template<typename Visitor>
  typename std::result_of<Visitor (Head)>::type
  operator () (Visitor const &v) const
  {
    if (head.tag == Index)
      return v (head.value);
    else
      return tail (v);
  }
};

template<std::size_t Index>
union variant_storage<Index>
{
  template<typename T>
  variant_storage (T const &)
  { static_assert (std::is_void<T>::value,
                   "Attempted to instantiate variant with incorrect type"); }

  struct any
  {
    template<typename T>
    operator T () const
    { assert (false); }
  };

  template<typename Visitor>
  any operator () (Visitor const &) const
  { assert (false); }
};


template<typename ...Types>
using variant = variant_storage<0, Types...>;
