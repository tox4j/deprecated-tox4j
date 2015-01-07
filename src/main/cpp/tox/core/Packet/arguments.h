#pragma once

#include "lwt/variant.h"
#include "Format.h"

#include <tuple>
#include <vector>


namespace tox
{
  namespace detail
  {
    template<typename Args, typename ...Contents>
    struct packet_argument_list;

    template<typename Flattened, typename List>
    struct flatten_tuple;

    template<typename Format>
    struct packet_arguments;


    template<typename Args>
    struct packet_argument_list<Args>
    {
      typedef Args type;
    };

    template<typename Args, typename Format, typename ...Rest>
    struct packet_argument_list<Args, Format, Rest...>
    {
      typedef typename packet_argument_list<
        std::tuple<Args, Format>,
        Rest...
      >::type type;
    };

    template<typename Args, typename IntegralType, IntegralType Value, typename ...Rest>
    struct packet_argument_list<Args, std::integral_constant<IntegralType, Value>, Rest...>
    {
      typedef typename packet_argument_list<Args, Rest...>::type type;
    };

    template<typename Args, typename IntegralType, IntegralType Value, std::size_t BitSize, typename ...Rest>
    struct packet_argument_list<Args, bitfield::member<std::integral_constant<IntegralType, Value>, BitSize>, Rest...>
    {
      typedef typename packet_argument_list<Args, Rest...>::type type;
    };

    template<typename Args, typename ...EncryptedContents, typename ...Rest>
    struct packet_argument_list<Args, encrypted<EncryptedContents...>, Rest...>
    {
      static_assert (sizeof... (Rest) == 0, "No data allowed after encrypted payload.");
      typedef typename packet_argument_list<
        Args,
        CryptoBox,
        EncryptedContents...
      >::type type;
    };

    template<typename Args, typename SizeType, typename ...Fields, typename ...Rest>
    struct packet_argument_list<Args, repeated<SizeType, Fields...>, Rest...>
    {
      typedef typename packet_argument_list<
        Args,
        std::vector<
          typename reduce_tuple<
            typename packet_arguments<
              PacketFormatTag<Fields...>
            >::type
          >::type
        >,
        Rest...
      >::type type;
    };

    template<typename Args, typename ...Choices, typename ...Rest>
    struct packet_argument_list<Args, choice<Choices...>, Rest...>
    {
      typedef typename packet_argument_list<
        Args,
        variant<
          typename packet_arguments<
            PacketFormatTag<Choices>
          >::type...
        >,
        Rest...
      >::type type;
    };

    template<typename Args, typename ...Members, typename ...Rest>
    struct packet_argument_list<Args, bitfield::type<Members...>, Rest...>
    {
      typedef typename packet_argument_list<
        Args,
        typename reduce_tuple<
          typename packet_arguments<
            PacketFormatTag<Members>
          >::type
        >::type...,
        Rest...
      >::type type;
    };

    template<typename Args, typename MemberType, std::size_t BitSize, typename ...Rest>
    struct packet_argument_list<Args, bitfield::member<MemberType, BitSize>, Rest...>
    {
      typedef typename packet_argument_list<
        Args,
        MemberType,
        Rest...
      >::type type;
    };

    // Flatten nested PacketFormatTags.
    template<typename Args, typename ...Contents, typename ...Rest>
    struct packet_argument_list<Args, PacketFormatTag<Contents...>, Rest...>
    {
      typedef typename packet_argument_list<
        Args,
        Contents...,
        Rest...
      >::type type;
    };


    template<typename ...Flattened>
    struct flatten_tuple<std::tuple<Flattened...>, std::tuple<>>
    {
      typedef std::tuple<Flattened...> type;
    };

    template<typename ...Flattened, typename Head, typename Tail>
    struct flatten_tuple<std::tuple<Flattened...>, std::tuple<Head, Tail>>
    {
      typedef typename flatten_tuple<std::tuple<Tail, Flattened...>, Head>::type type;
    };

    template<typename ...Flattened, typename Head>
    struct flatten_tuple<std::tuple<Flattened...>, std::tuple<Head, std::tuple<>>>
    {
      typedef typename flatten_tuple<std::tuple<Flattened...>, Head>::type type;
    };


    template<typename ...Contents>
    struct packet_arguments<PacketFormatTag<Contents...>>
    {
      typedef typename flatten_tuple<
        std::tuple<>,
        typename packet_argument_list<
          std::tuple<>,
          Contents...
        >::type
      >::type type;
    };
  }
}
