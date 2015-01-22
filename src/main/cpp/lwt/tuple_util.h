#pragma once

#include <tuple>


/*******************************************************************************
 * :: reduce_tuple
 ******************************************************************************/


template<typename T>
struct reduce_tuple
{
  typedef T type;
};

template<typename T>
struct reduce_tuple<std::tuple<T>>
{
  typedef T type;
};


/*******************************************************************************
 * :: make_seq
 ******************************************************************************/


template<std::size_t ...>
struct seq { };

template<std::size_t N, std::size_t ...S>
struct make_seq_t : make_seq_t<N - 1, N - 1, S...> { };

template<std::size_t ...S>
struct make_seq_t<0, S...>
{ typedef seq<S...> type; };

template<std::size_t N>
using make_seq = typename make_seq_t<N>::type;


/*******************************************************************************
 * :: is_in_tuple
 ******************************************************************************/


template<typename V, typename T>
struct is_in_tuple;

template<typename V, typename T0, typename ...T>
struct is_in_tuple<V, std::tuple<T0, T...>>
  : is_in_tuple<V, std::tuple<T...>>
{ };

template<typename V, typename... T>
struct is_in_tuple<V, std::tuple<V, T...>>
  : std::true_type
{ };

template<typename V>
struct is_in_tuple<V, std::tuple<>>
  : std::false_type
{ };


/*******************************************************************************
 * :: tuple_append
 ******************************************************************************/


template<typename Tuple, typename ...Types>
struct tuple_append;

template<typename ...TupleTypes, typename ...Types>
struct tuple_append<std::tuple<TupleTypes...>, Types...>
{
  typedef std::tuple<TupleTypes..., Types...> type;
};


#if 0
template<std::size_t ...LhsS, std::size_t ...RhsS, typename ...Lhs, typename ...Rhs>
std::tuple<Lhs..., Rhs...>
tuple_cat (seq<LhsS...>, seq<RhsS...>, std::tuple<Lhs...> &&lhs, std::tuple<Rhs...> &&rhs)
{
  return std::tuple<Lhs..., Rhs...> (std::move (std::get<LhsS> (lhs))...,
                                     std::move (std::get<RhsS> (rhs))...);
}


template<std::size_t ...LhsS, typename ...Lhs, typename ...Rhs>
std::tuple<Lhs..., Rhs...>
tuple_cat (seq<LhsS...>, std::tuple<Lhs...> &&lhs, Rhs &&...rhs)
{
  return std::tuple<Lhs..., Rhs...> (std::move (std::get<LhsS> (lhs))...,
                                     std::move (rhs)...);
}


template<typename ...Lhs, typename ...Rhs>
std::tuple<Lhs..., Rhs...>
operator + (std::tuple<Lhs...> &&lhs, std::tuple<Rhs...> &&rhs)
{
  return tuple_cat (make_seq<sizeof... (Lhs)> (),
                    make_seq<sizeof... (Rhs)> (),
                    std::move (lhs),
                    std::move (rhs));
}


template<typename ...Lhs, typename Rhs>
std::tuple<Lhs..., Rhs>
operator + (std::tuple<Lhs...> &&lhs, Rhs &&rhs)
{
  return tuple_cat (make_seq<sizeof... (Lhs)> (),
                    std::move (lhs),
                    std::move (rhs));
}
#endif



/*******************************************************************************
 * :: tuple_types_distinct
 ******************************************************************************/


namespace detail
{
  template<typename T, typename Tuple>
  struct type_occurrences;

  template<typename T, typename Type, typename ...Types>
  struct type_occurrences<T, std::tuple<Type, Types...>>
  {
    static std::size_t const value =
      std::is_same<T, Type>::value + type_occurrences<T, std::tuple<Types...>>::value;
  };

  template<typename T>
  struct type_occurrences<T, std::tuple<>>
  {
    static std::size_t const value = 0;
  };


  template<typename Tuple, typename ...Types>
  struct tuple_types_distinct;

  template<typename Tuple, typename T, typename ...Types>
  struct tuple_types_distinct<Tuple, T, Types...>
  {
    static bool const value = type_occurrences<T, Tuple>::value == 1
                           && tuple_types_distinct<Tuple, Types...>::value;
  };

  template<typename Tuple>
  struct tuple_types_distinct<Tuple>
    : std::true_type
  { };
}


template<typename Tuple>
struct tuple_types_distinct;

template<typename ...Types>
struct tuple_types_distinct<std::tuple<Types...>>
  : detail::tuple_types_distinct<std::tuple<Types...>, Types...>
{ };


/*******************************************************************************
 * :: common_result_of
 ******************************************************************************/


template<typename ...Types>
struct common_type;

template<typename Type1, typename Type2, typename ...Types>
struct common_type<Type1, Type2, Types...>
  : common_type<typename common_type<Type1, Type2>::type, Types...>
{ };

template<typename Type1, typename Type2>
struct common_type<Type1, Type2>
  : std::common_type<Type1, Type2>
{ };

template<>
struct common_type<void, void>
{ typedef void type; };


template<typename Matchers, typename ...Types>
struct common_result_of;

template<typename ...Matchers, typename ...Types>
struct common_result_of<std::tuple<Matchers...>, Types...>
  : common_type<
      typename common_result_of<std::tuple<Matchers>, Types>::type...
    >
{
};

template<typename Matcher, typename Type>
struct common_result_of<std::tuple<Matcher>, Type>
  : std::result_of<Matcher (Type)>
{
};
