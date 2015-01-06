#pragma once

#include "tox/core/CryptoBox.h"
#include "tox/core/Message.h"

#include "tox/core/tuple_util.h"
#include "tox/core/variant.h"

#include "Format.h"
#include "arguments.h"

#include <limits>


namespace tox
{
  namespace detail
  {
    template<typename Fmts, typename ...Args>
    struct write_packet;

    template<typename Fmts, typename ...Args>
    struct write_packet_recurse;


    template<typename ...Fmts, typename ...Args>
    struct write_packet<PacketFormatTag<Fmts...>, Args...>
    {
      template<typename MessageFormat>
      static void write (MessageFormat &packet, Args const &...args)
      {
        write_packet_recurse<PacketFormatTag<Fmts...>, Args...>::write (packet, args...);
      }

      template<typename MessageFormat>
      static void write (Nonce const &nonce, MessageFormat &packet, Args const &...args)
      {
        write_packet_recurse<PacketFormatTag<Fmts...>, Args...>::write (nonce, packet, args...);
      }
    };

    // Flatten PacketFormatTags.
    template<typename ...InnerFmts, typename ...Fmts, typename ...Args>
    struct write_packet<PacketFormatTag<PacketFormatTag<InnerFmts...>, Fmts...>, Args...>
    {
      template<typename MessageFormat>
      static void write (MessageFormat &packet, Args const &...args)
      {
        write_packet<PacketFormatTag<InnerFmts..., Fmts...>, Args...>::write (packet, args...);
      }
    };

    template<typename IntegralType, IntegralType Value, typename ...Fmts, typename ...Args>
    struct write_packet<PacketFormatTag<std::integral_constant<IntegralType, Value>, Fmts...>, Args...>
    {
      template<typename MessageFormat>
      static void write (MessageFormat &packet, Args const &...args)
      {
        packet << Value;
        write_packet<PacketFormatTag<Fmts...>, Args...>::write (packet, args...);
      }

      template<typename MessageFormat>
      static void write (Nonce const &nonce, MessageFormat &packet, Args const &...args)
      {
        packet << Value;
        write_packet<PacketFormatTag<Fmts...>, Args...>::write (nonce, packet, args...);
      }
    };

    template<typename ...Fmts, typename ...Args>
    struct write_packet<PacketFormatTag<encrypted<Fmts...>>, CryptoBox, Args...>
    {
      static void write (Nonce const &nonce, CipherText &packet, CryptoBox const &box, Args const &...args)
      {
        PlainText plain;
        write_packet<PacketFormatTag<Fmts...>, Args...>::write (plain, args...);
        packet << box.encrypt (plain, nonce);
      }
    };

    template<typename ...Fmts, typename ...Args>
    struct write_packet<PacketFormatTag<Nonce, Fmts...>, Nonce, Args...>
    {
      static void write (PlainText &packet, Nonce const &nonce, Args const &...args)
      {
        packet << nonce;
        write_packet<PacketFormatTag<Fmts...>, Args...>::write (packet, args...);
      }

      static void write (CipherText &packet, Nonce const &nonce, Args const &...args)
      {
        packet << nonce;
        write_packet<PacketFormatTag<Fmts...>, Args...>::write (nonce, packet, args...);
      }
    };

    template<typename SizeType, typename ...Fields, typename ...Fmts, typename ...Members, typename ...Args>
    struct write_packet<PacketFormatTag<repeated<SizeType, Fields...>, Fmts...>, std::vector<std::tuple<Members...>>, Args...>
    {
      template<std::size_t ...S>
      static void write (seq<S...>, PlainText &packet, std::tuple<Members...> const &field)
      {
        write_packet<PacketFormatTag<Fields...>, Members...>::write (packet, std::get<S> (field)...);
      }

      static void write (PlainText &packet, std::vector<std::tuple<Members...>> const &fields, Args const &...args)
      {
        assert (fields.size () <= std::numeric_limits<SizeType>::max ());
        packet << static_cast<SizeType> (fields.size ());
        for (std::tuple<Members...> const &field : fields)
          write (make_seq<sizeof... (Members)> (), packet, field);
        write_packet<PacketFormatTag<Fmts...>, Args...>::write (packet, args...);
      }
    };

    template<typename Choices, typename ...Variants>
    struct write_choice;

    template<typename Choice, typename ...Choices, typename Variant, typename ...Variants>
    struct write_choice<choice<Choice, Choices...>, Variant, Variants...>
      : write_choice<choice<Choices...>, Variants...>
    {
      using write_choice<choice<Choices...>, Variants...>::write_choice;
      using write_choice<choice<Choices...>, Variants...>::operator ();

      void operator () (Variant const &variant) const
      {
        write_packet<PacketFormatTag<Choice>, Variant>::write (*this->packet_, variant);
      }
    };

    template<>
    struct write_choice<choice<>>
    {
      write_choice (PlainText &packet)
        : packet_ (&packet)
      { }

    protected:
      void operator () () const;

      PlainText *packet_;
    };

    template<typename ...Choices, typename ...Fmts, typename ...Variants, typename ...Args>
    struct write_packet<PacketFormatTag<choice<Choices...>, Fmts...>, variant<Variants...>, Args...>
    {
      static void write (PlainText &packet, variant<Variants...> const &choices, Args const &...args)
      {
        choices (write_choice<choice<Choices...>, Variants...> (packet));
        write_packet<PacketFormatTag<Fmts...>, Args...>::write (packet, args...);
      }
    };


    template<typename Fmts, typename ...Args>
    struct write_packet_bits
    {
      static_assert (std::is_same<Fmts, PacketFormatTag<>>::value,
                     "Base case for bitfield template must be at the end of the format list");
      static_assert (sizeof... (Args) == 0,
                     "Base case for bitfield template must be at the end of the argument list");

      template<typename ...Bitfield>
      struct inner
      {
        static_assert (sizeof... (Bitfield) == 0,
                       "Base case for inner bitfield template must be empty.");

        template<std::size_t Offset>
        static void write (PlainText &packet, Args const &...args)
        {
          static_assert (Offset % 8 == 0, "Bitfields do not end on a byte boundary");
          write_packet<Fmts, Args...>::write (packet, args...);
        }
      };

      template<typename IntegralType, IntegralType Value, std::size_t BitSize, typename ...Bitfield>
      struct inner<
        bitfield::member<std::integral_constant<IntegralType, Value>, BitSize>, Bitfield...
      >
      {
        template<std::size_t Offset>
        static void write (PlainText &packet, Args const &...args)
        {
          packet << Value;
          packet.shift_left (Offset, BitSize);
          inner<Bitfield...>::
            template write<Offset + BitSize> (packet, args...);
        }
      };
    };


    template<typename Fmts, typename Field, typename ...Args>
    struct write_packet_bits<Fmts, Field, Args...>
    {
      template<typename ...Bitfield>
      struct inner
      {
        static_assert (sizeof... (Bitfield) == 0,
                       "Base case for inner bitfield template must be empty.");

        template<std::size_t Offset>
        static void write (PlainText &packet, Field const &field, Args const &...args)
        {
          static_assert (Offset % 8 == 0, "Bitfields do not end on a byte boundary");
          write_packet<Fmts, Field, Args...>::write (packet, field, args...);
        }
      };

      template<typename IntegralType, IntegralType Value, std::size_t BitSize, typename ...Bitfield>
      struct inner<
        bitfield::member<std::integral_constant<IntegralType, Value>, BitSize>, Bitfield...
      >
      {
        template<std::size_t Offset>
        static void write (PlainText &packet, Field const &field, Args const &...args)
        {
          packet << Value;
          packet.shift_left (Offset, BitSize);
          inner<Bitfield...>::
            template write<Offset + BitSize> (packet, field, args...);
        }
      };

      template<typename MemberType, std::size_t BitSize, typename ...Bitfield>
      struct inner<
        bitfield::member<MemberType, BitSize>, Bitfield...
      >
      {
        template<std::size_t Offset>
        static void write (PlainText &packet, Field const &field, Args const &...args)
        {
          write_packet<PacketFormatTag<MemberType>, Field>::write (packet, field);
          packet.shift_left (Offset, BitSize);
          write_packet_bits<Fmts, Args...>::
            template inner<Bitfield...>::
            template write<Offset + BitSize> (packet, args...);
        }
      };
    };

    template<typename ...Members, typename ...Fmts, typename ...Args>
    struct write_packet<PacketFormatTag<bitfield::type<Members...>, Fmts...>, Args...>
    {
      static void write (PlainText &packet, Args const &...args)
      {
        write_packet_bits<PacketFormatTag<Fmts...>, Args...>::
          template inner<Members...>::
          template write<0> (packet, args...);
      }
    };


    template<typename ...Members, typename ...Fmts, typename ...Fields, typename ...Args>
    struct write_packet<PacketFormatTag<bitfield::type<Members...>, Fmts...>, std::tuple<Fields...>, Args...>
    {
      template<std::size_t ...S>
      static void write_bitfields (seq<S...>, PlainText &packet, std::tuple<Fields...> const &fields, Args const &...args)
      {
        write_packet_bits<PacketFormatTag<Fmts...>, Fields..., Args...>::
          template inner<Members...>::
          template write<0> (packet, std::get<S> (fields)..., args...);
      }

      static void write (PlainText &packet, std::tuple<Fields...> const &fields, Args const &...args)
      {
        write_bitfields (make_seq<sizeof... (Fields)> (), packet, fields, args...);
      }
    };


    template<>
    struct write_packet_recurse<PacketFormatTag<>>
    {
      static void write (PlainText &/*packet*/) { }
      static void write (CipherText &/*packet*/) { }
      static void write (Nonce const &/*nonce*/, CipherText &/*packet*/) { }
    };

    template<typename FirstFmt, typename ...Fmts, typename FirstArg, typename ...Args>
    struct write_packet_recurse<PacketFormatTag<FirstFmt, Fmts...>, FirstArg, Args...>
    {
      template<typename MessageFormat>
      static void write (MessageFormat &packet, FirstArg const &arg, Args const &...args)
      {
        packet << arg;
        write_packet<PacketFormatTag<Fmts...>, Args...>::write (packet, args...);
      }

      template<typename MessageFormat>
      static void write (Nonce const &nonce, MessageFormat &packet, FirstArg const &arg, Args const &...args)
      {
        packet << arg;
        write_packet<PacketFormatTag<Fmts...>, Args...>::write (nonce, packet, args...);
      }
    };
  }


  namespace detail
  {
    template<typename Format, typename ArgsTuple>
    struct packet_encoder;

    template<typename Format, typename ...Args>
    struct packet_encoder<Format, std::tuple<Args...>>
    {
    protected:
      static void encode (CipherText &packet, Args const &...contents)
      {
        write_packet<Format, Args...>::write (packet, contents...);
      }
    };
  }
}
