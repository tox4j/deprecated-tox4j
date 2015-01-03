#pragma once

#include <cassert>
#include <cstddef>
#include <type_traits>
#include <utility>


template<typename T>
struct variant_member
{
  std::size_t tag;
  T value;
};

template<typename ...Types>
union variant;

template<typename Head, typename ...Tail>
union variant<Head, Tail...>
{
  static std::size_t const index = sizeof... (Tail);

  typedef variant_member<Head> head_type;
  typedef variant<Tail...> tail_type;

  head_type head;
  tail_type tail;

  template<typename T>
  variant (T const &value)
    : tail (value)
  { }

  variant (Head const &head)
    : head { index, head }
  { }

  variant (variant &&rhs)
  {
    if (rhs.head.tag == index)
      new (&head) head_type (std::move (rhs.head));
    else
      new (&tail) tail_type (std::move (rhs.tail));
    assert (head.tag == rhs.head.tag);
  }

  variant (variant const &rhs)
  {
    if (rhs.head.tag == index)
      new (&head) head_type (rhs.head);
    else
      new (&tail) tail_type (rhs.tail);
    assert (head.tag == rhs.head.tag);
  }

  ~variant ()
  {
    if (head.tag == index)
      head.value.~Head ();
    else
      tail.~tail_type ();
  }

  template<typename Visitor>
  typename std::result_of<Visitor (Head)>::type
  operator () (Visitor const &v) const
  {
    return visit<typename std::result_of<Visitor (Head)>::type> (v);
  }

  template<typename Result, typename Visitor>
  Result visit (Visitor const &v) const
  {
    if (head.tag == index)
      return v (head.value);
    else
      return tail.template visit<Result> (v);
  }
};

template<>
union variant<>
{
  template<typename T>
  variant (T const &)
  { static_assert (std::is_void<T>::value,
                   "Attempted to instantiate variant with incorrect type"); }

  template<typename Result, typename Visitor>
  Result visit (Visitor const &) const
  { assert (false); }
};
