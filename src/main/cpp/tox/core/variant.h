#pragma once

#include <cassert>
#include <cstddef>

#include <functional>
#include <type_traits>
#include <utility>

#include "tuple_util.h"


template<typename Result, typename ...Types>
struct variant_visitor;

template<typename Result, typename Head, typename ...Tail>
struct variant_visitor<Result, Head, Tail...>
  : variant_visitor<Result, Tail...>
{
  template<typename HeadFun, typename ...TailFun>
  variant_visitor (HeadFun const &head_fun, TailFun const &...tail_fun)
    : variant_visitor<Result, Tail...> (tail_fun...)
    , head_fun_ (head_fun)
  {
  }

  using variant_visitor<Result, Tail...>::operator ();
  Result operator () (Head const &value) const
  {
    return head_fun_ (value);
  }

private:
  std::function<Result (Head)> head_fun_;
};

template<typename Result>
struct variant_visitor<Result>
{
protected:
  Result operator () () const;
};


template<typename Tag, typename T>
struct variant_member
{
  Tag tag;
  T value;
};

template<typename Tag, typename ...Types>
union variant_storage;

template<typename Tag, typename Head, typename ...Tail>
union variant_storage<Tag, Head, Tail...>
{
  template<typename, typename ...>
  friend union variant_storage;

  static Tag const index = sizeof... (Tail);
  static Tag const invalid_index = -1;
  static_assert (static_cast<std::size_t> (index) == sizeof... (Tail) &&
                 index < invalid_index,
                 "Tag type is too small for this variant");
  static_assert (tuple_types_distinct<std::tuple<Head, Tail...>>::value,
                 "Variant members must all be distinct types");

  template<typename Result>
  using visitor = variant_visitor<Result, Head, Tail...>;

  typedef variant_member<Tag, Head> head_type;
  typedef variant_storage<Tag, Tail...> tail_type;

  head_type head;
  tail_type tail;

  template<typename T>
  explicit variant_storage (T const &value)
    : tail (value)
  { }

  explicit variant_storage (Head const &head)
    : head { index, head }
  { }

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

  bool empty () const
  {
    return head.tag == invalid_index;
  }

  void clear ()
  {
    this->~variant_storage ();
    head.tag = invalid_index;
  }

  template<typename Visitor, typename ...Args>
  typename std::result_of<Visitor (Head, Args...)>::type
  operator () (Visitor const &v, Args const &...args) const
  {
    return dispatch<typename std::result_of<Visitor (Head, Args...)>::type> (v, args...);
  }

private:
  template<typename Result>
  struct visitor_accept
  {
    Result operator >>= (visitor<Result> const &v) const
    {
      return variant_.dispatch<Result> (v);
    }

    variant_storage const &variant_;
  };

public:
  template<typename Result>
  visitor_accept<Result> visit () const
  {
    return visitor_accept<Result> { *this };
  }

  visitor_accept<void> visit () const
  {
    return visit<void> ();
  }

  template<typename Result>
  Result visit (visitor<Result> const &v) const
  {
    return dispatch<Result> (v);
  }

  void visit (visitor<void> const &v) const
  {
    return visit<void> (v);
  }

private:
  template<typename Result, typename Visitor, typename ...Args>
  Result dispatch (Visitor const &v, Args const &...args) const
  {
    if (head.tag == index)
      return v (head.value, args...);
    else
      return tail.template dispatch<Result> (v, args...);
  }
};

template<typename Tag>
union variant_storage<Tag>
{
  template<typename, typename ...>
  friend union variant_storage;

private:
  template<typename T>
  explicit variant_storage (T const &)
  { static_assert (std::is_void<T>::value,
                   "Attempted to instantiate variant with incorrect type"); }

  template<typename Result, typename Visitor, typename ...Args>
  Result dispatch (Visitor const &, Args const &...) const
  { assert (!"Attempted to visit empty variant"); }
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
