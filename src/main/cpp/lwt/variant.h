#pragma once

#include <cassert>
#include <cstddef>

#include <functional>
#include <type_traits>
#include <utility>

#include "tuple_util.h"


template<typename TagType, typename ...Types>
union variant_storage;

template<typename TagType, typename Head, typename ...Tail>
union variant_storage<TagType, Head, Tail...>
{
private:
  template<typename, typename ...>
  friend union variant_storage;

  static TagType const index = sizeof... (Tail);
  static TagType const invalid_index = -1;
  static_assert (std::is_unsigned<TagType>::value,
                 "Tag type must be an unsigned integral type");
  static_assert (static_cast<std::size_t> (index) == sizeof... (Tail) &&
                 index < invalid_index,
                 "Tag type is too small for this variant");
  static_assert (tuple_types_distinct<std::tuple<Head, Tail...>>::value,
                 "Variant members must all be distinct types");

  struct head_type
  {
    TagType tag;
    Head value;
  };

  typedef variant_storage<TagType, Tail...> tail_type;

  head_type head;
  tail_type tail;

public:
  template<typename T>
  explicit variant_storage (T const &value)
    : tail (value)
  { }

  explicit variant_storage (Head &&head)
    : head { index, std::move (head) }
  { }

  explicit variant_storage (Head const &head)
    : head { index, head }
  { }

  template<typename T>
  variant_storage &operator = (T &&value)
  {
    // Destroy self.
    this->~variant_storage ();
    // Renew self.
    new (static_cast<void *> (this)) variant_storage (std::move (value));
    return *this;
  }

  template<typename T>
  variant_storage &operator = (T const &value)
  {
    // Destroy self.
    this->~variant_storage ();
    // Renew self.
    new (static_cast<void *> (this)) variant_storage (value);
    return *this;
  }

  variant_storage ()
  {
    head.tag = invalid_index;
  }

  variant_storage (variant_storage &&rhs)
  {
    if (rhs.head.tag == index)
      new (static_cast<void *> (&head)) head_type (std::move (rhs.head));
    else
      new (static_cast<void *> (&tail)) tail_type (std::move (rhs.tail));
    assert (head.tag == rhs.head.tag);
  }

  variant_storage (variant_storage const &rhs)
  {
    if (rhs.head.tag == index)
      new (static_cast<void *> (&head)) head_type (rhs.head);
    else
      new (static_cast<void *> (&tail)) tail_type (rhs.tail);
    assert (head.tag == rhs.head.tag);
  }

  ~variant_storage ()
  {
    if (head.tag == index)
      head.value.~Head ();
    else
      tail.~tail_type ();
  }

  template<typename T>
  bool is () const
  {
    if (head.tag == index && std::is_same<T, Head>::value)
      return true;
    else
      return tail.template is<T> ();
  }

  bool empty () const
  {
    return head.tag == invalid_index;
  }

  void clear ()
  {
    this->~variant_storage ();
    head.tag = invalid_index;
  }

  template<typename ...Matchers>
  typename common_result_of<std::tuple<Matchers...>, Head, Tail...>::type
  match (Matchers const &...matchers) &&
  {
    typedef typename common_result_of<std::tuple<Matchers...>, Head, Tail...>::type return_type;
    return std::move (*this).template dispatch_match<return_type> (matchers...);
  }

  template<typename ...Matchers>
  typename common_result_of<std::tuple<Matchers...>, Head, Tail...>::type
  match (Matchers const &...matchers) const &
  {
    typedef typename common_result_of<std::tuple<Matchers...>, Head, Tail...>::type return_type;
    return dispatch_match<return_type> (matchers...);
  }

  template<typename Visitor, typename ...Args>
  typename std::result_of<Visitor (Head, Args...)>::type
  visit (Visitor const &v, Args const &...args) &&
  {
    return std::move (*this).template dispatch_visit<typename std::result_of<Visitor (Head, Args...)>::type> (v, args...);
  }

  template<typename Visitor, typename ...Args>
  typename std::result_of<Visitor (Head, Args...)>::type
  visit (Visitor const &v, Args const &...args) const &
  {
    return dispatch_visit<typename std::result_of<Visitor (Head, Args...)>::type> (v, args...);
  }

private:
  template<typename Result, typename Matcher, typename ...Matchers>
  Result dispatch_match (Matcher const &first, Matchers const &...matchers) &&
  {
    if (head.tag == index)
      return first (std::move (head.value));
    else
      return std::move (tail).template dispatch_match<Result> (matchers...);
  }

  template<typename Result, typename Matcher, typename ...Matchers>
  Result dispatch_match (Matcher const &first, Matchers const &...matchers) const &
  {
    if (head.tag == index)
      return first (head.value);
    else
      return tail.template dispatch_match<Result> (matchers...);
  }

  template<typename Result, typename Visitor, typename ...Args>
  Result dispatch_visit (Visitor const &v, Args const &...args) &&
  {
    if (head.tag == index)
      return v (std::move (head.value), args...);
    else
      return tail.template dispatch_visit<Result> (v, args...);
  }

  template<typename Result, typename Visitor, typename ...Args>
  Result dispatch_visit (Visitor const &v, Args const &...args) const &
  {
    if (head.tag == index)
      return v (head.value, args...);
    else
      return tail.template dispatch_visit<Result> (v, args...);
  }
};

template<typename TagType>
union variant_storage<TagType>
{
private:
  template<typename, typename ...>
  friend union variant_storage;

  template<typename T>
  explicit variant_storage (T const &)
  { static_assert (std::is_void<T>::value,
                   "Attempted to instantiate variant with incorrect type"); }

  template<typename Result>
  Result dispatch_match () const
  { assert (!"Attempted to match on empty variant"); }

  template<typename Result, typename Visitor, typename ...Args>
  Result dispatch_visit (Visitor const &, Args const &...) const
  { assert (!"Attempted to visit empty variant"); }

  template<typename T>
  bool is () const
  { return false; }
};


template<typename ...Types>
using variant = variant_storage<unsigned char, Types...>;


template<std::size_t Index, typename Variant>
struct variant_type;

template<std::size_t Index, typename T, typename ...Types>
struct variant_type<Index, variant<T, Types...>>
  : variant_type<Index - 1, variant<Types...>>
{ };

template<typename T, typename ...Types>
struct variant_type<0, variant<T, Types...>>
{ typedef T type; };
