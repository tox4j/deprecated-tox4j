#pragma once

#include <cstddef>

namespace tox
{
  template<typename ...Contents>
  struct encrypted
  { };

  template<typename ...Choices>
  struct choice
  { };

  template<typename SizeType, typename ...Fields>
  struct repeated
  { };

  namespace bitfield
  {
    template<typename ...Members>
    struct type
    { };

    template<typename MemberType, std::size_t BitSize>
    struct member
    { };
  }


  template<typename ...Contents>
  struct PacketFormatTag
  { };
}
