#pragma once

#include <tuple>


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

template<std::size_t ...>
struct seq { };

template<std::size_t N, std::size_t ...S>
struct make_seq_t : make_seq_t<N - 1, N - 1, S...> { };

template<std::size_t ...S>
struct make_seq_t<0, S...>
{ typedef seq<S...> type; };

template<std::size_t N>
using make_seq = typename make_seq_t<N>::type;
