#pragma once

#include <cstddef>

#include <type_traits>

namespace tox
{
  namespace detail
  {
    struct tag_operator
    { };
  }

  template<typename ...Contents>
  struct encrypted
    : detail::tag_operator
  { };

  template<typename ...Choices>
  struct choice
    : detail::tag_operator
  { };

  template<typename SizeType, typename ...Records>
  struct repeated
    : detail::tag_operator
  { };

  namespace bitfield
  {
    template<typename ...Members>
    struct type
      : detail::tag_operator
    { };

    template<typename MemberType, std::size_t BitSize>
    struct member
      : detail::tag_operator
    { };
  }


  template<typename ...Contents>
  struct PacketFormatTag
    : detail::tag_operator
  { };


  // TODO: flatten inner PacketFormatTags
  namespace detail
  {
    template<typename Format>
    struct reduce_packet_format
    {
      static_assert (!std::is_base_of<tag_operator, Format>::value,
                     "Unimplemented reduce_packet_format");
      typedef Format type;
    };


    template<typename ...Contents>
    struct reduce_packet_format<PacketFormatTag<Contents...>>
    {
      typedef PacketFormatTag<typename reduce_packet_format<Contents>::type...> type;
    };


    template<typename Singleton>
    struct reduce_packet_format<PacketFormatTag<Singleton>>
      : reduce_packet_format<Singleton>
    {
    };


    template<typename ...Contents>
    struct reduce_packet_format<encrypted<Contents...>>
    {
      typedef encrypted<typename reduce_packet_format<Contents>::type...> type;
    };


    template<typename ...Choices>
    struct reduce_packet_format<choice<Choices...>>
    {
      typedef choice<typename reduce_packet_format<Choices>::type...> type;
    };


    template<typename SizeType, typename ...Records>
    struct reduce_packet_format<repeated<SizeType, Records...>>
    {
      typedef repeated<SizeType, typename reduce_packet_format<Records>::type...> type;
    };


    template<typename... Members>
    struct reduce_packet_format<bitfield::type<Members...>>
    {
      typedef bitfield::type<typename reduce_packet_format<Members>::type...> type;
    };


    template<typename MemberType, std::size_t BitSize>
    struct reduce_packet_format<bitfield::member<MemberType, BitSize>>
    {
      typedef bitfield::member<typename reduce_packet_format<MemberType>::type, BitSize> type;
    };
  }
}
