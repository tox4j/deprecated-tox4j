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
    template<typename Crypto, typename Fmts, typename ...Args>
    struct write_packet;

    template<typename Crypto, typename Fmts, typename ...Args>
    struct write_packet_recurse;


    template<typename ...Crypto, typename ...Fmts, typename ...Args>
    struct write_packet<std::tuple<Crypto...>, PacketFormatTag<Fmts...>, Args...>
    {
      template<typename MessageFormat>
      static void write (Crypto const &...crypto, MessageFormat &packet, Args const &...args)
      {
        write_packet_recurse<std::tuple<Crypto...>, PacketFormatTag<Fmts...>, Args...>::write (crypto..., packet, args...);
      }
    };

    // Flatten PacketFormatTags.
    template<typename ...Crypto, typename ...InnerFmts, typename ...Fmts, typename ...Args>
    struct write_packet<std::tuple<Crypto...>, PacketFormatTag<PacketFormatTag<InnerFmts...>, Fmts...>, Args...>
    {
      template<typename MessageFormat>
      static void write (MessageFormat &packet, Args const &...args)
      {
        write_packet<std::tuple<Crypto...>, PacketFormatTag<InnerFmts..., Fmts...>, Args...>::write (packet, args...);
      }
    };


    template<typename ...Crypto, typename ...InnerFmts, typename ...Fmts, typename ...InnerArgs, typename ...Args>
    struct write_packet<std::tuple<Crypto...>, PacketFormatTag<PacketFormatTag<InnerFmts...>, Fmts...>, std::tuple<InnerArgs...>, Args...>
    {
      template<typename MessageFormat, std::size_t ...S>
      static void write (seq<S...>, Crypto const &...crypto, MessageFormat &packet, std::tuple<InnerArgs...> const &inner_args, Args const &...args)
      {
        write_packet<std::tuple<Crypto...>, PacketFormatTag<InnerFmts..., Fmts...>, InnerArgs..., Args...>::write (crypto..., packet, std::get<S> (inner_args)..., args...);
      }

      template<typename MessageFormat>
      static void write (Crypto const &...crypto, MessageFormat &packet, std::tuple<InnerArgs...> const &inner_args, Args const &...args)
      {
        write (make_seq<sizeof... (InnerArgs)> (), crypto..., packet, inner_args, args...);
      }
    };


    template<typename ...Crypto, typename IntegralType, IntegralType Value, typename ...Fmts, typename ...Args>
    struct write_packet<std::tuple<Crypto...>, PacketFormatTag<std::integral_constant<IntegralType, Value>, Fmts...>, Args...>
    {
      template<typename MessageFormat>
      static void write (Crypto const &...crypto, MessageFormat &packet, Args const &...args)
      {
        packet << Value;
        write_packet<std::tuple<Crypto...>, PacketFormatTag<Fmts...>, Args...>::write (crypto..., packet, args...);
      }
    };

    template<typename ...Crypto, typename ...Fmts, typename ...Args>
    struct write_packet<std::tuple<Nonce const &, Crypto...>, PacketFormatTag<encrypted<Fmts...>>, CryptoBox, Args...>
    {
      static void write (Nonce const &nonce, Crypto const &...crypto, CipherText &packet, CryptoBox const &box, Args const &...args)
      {
        static_assert (sizeof... (crypto) == 0,
                       "Unexpected extra crypto arguments");
        PlainText plain;
        write_packet<std::tuple<>, PacketFormatTag<Fmts...>, Args...>::write (plain, args...);
        packet << box.encrypt (plain, nonce);
      }
    };

    template<typename ...Crypto, typename ...Fmts, typename ...Args>
    struct write_packet<std::tuple<Crypto...>, PacketFormatTag<Nonce, Fmts...>, Nonce, Args...>
    {
      static void write (Crypto const &...crypto, PlainText &packet, Nonce const &nonce, Args const &...args)
      {
        packet << nonce;
        write_packet<std::tuple<Crypto...>, PacketFormatTag<Fmts...>, Args...>::write (crypto..., packet, args...);
      }

      static void write (Crypto const &...crypto, CipherText &packet, Nonce const &nonce, Args const &...args)
      {
        packet << nonce;
        write_packet<std::tuple<Nonce const &, Crypto...>, PacketFormatTag<Fmts...>, Args...>::write (nonce, crypto..., packet, args...);
      }
    };

    template<typename ...Crypto, typename SizeType, typename ...Fields, typename ...Fmts, typename ...Members, typename ...Args>
    struct write_packet<std::tuple<Crypto...>, PacketFormatTag<repeated<SizeType, Fields...>, Fmts...>, std::vector<std::tuple<Members...>>, Args...>
    {
      template<std::size_t ...S, typename MessageFormat>
      static void write (seq<S...>, MessageFormat &packet, std::tuple<Members...> const &field)
      {
        write_packet<std::tuple<Crypto...>, PacketFormatTag<Fields...>, Members...>::write (packet, std::get<S> (field)...);
      }

      template<typename MessageFormat>
      static void write (Crypto const &...crypto, MessageFormat &packet, std::vector<std::tuple<Members...>> const &fields, Args const &...args)
      {
        assert (fields.size () <= std::numeric_limits<SizeType>::max ());
        packet << static_cast<SizeType> (fields.size ());
        for (std::tuple<Members...> const &field : fields)
          write (make_seq<sizeof... (Members)> (), packet, field);
        write_packet<std::tuple<Crypto...>, PacketFormatTag<Fmts...>, Args...>::write (crypto..., packet, args...);
      }
    };

    template<typename Crypto, typename MessageFormat, typename Choices, typename ...Variants>
    struct write_choice;

    template<typename ...Crypto, typename MessageFormat, typename Choice, typename ...Choices, typename Variant, typename ...Variants>
    struct write_choice<std::tuple<Crypto...>, MessageFormat, choice<Choice, Choices...>, Variant, Variants...>
      : write_choice<std::tuple<Crypto...>, MessageFormat, choice<Choices...>, Variants...>
    {
      using write_choice<std::tuple<Crypto...>, MessageFormat, choice<Choices...>, Variants...>::write_choice;
      using write_choice<std::tuple<Crypto...>, MessageFormat, choice<Choices...>, Variants...>::operator ();

      void operator () (Variant const &variant, Crypto const &...crypto) const
      {
        write_packet<std::tuple<Crypto...>, PacketFormatTag<Choice>, Variant>::write (crypto..., *this->packet_, variant);
      }
    };

    template<typename ...Crypto, typename MessageFormat>
    struct write_choice<std::tuple<Crypto...>, MessageFormat, choice<>>
    {
      write_choice (MessageFormat &packet)
        : packet_ (&packet)
      { }

    protected:
      void operator () () const;

      MessageFormat *packet_;
    };

    template<typename ...Crypto, typename ...Choices, typename ...Fmts, typename ...Variants, typename ...Args>
    struct write_packet<std::tuple<Crypto...>, PacketFormatTag<choice<Choices...>, Fmts...>, variant<Variants...>, Args...>
    {
      template<typename MessageFormat>
      static void write (Crypto const &...crypto, MessageFormat &packet, variant<Variants...> const &choices, Args const &...args)
      {
        choices (write_choice<std::tuple<Crypto...>, MessageFormat, choice<Choices...>, Variants...> (packet), crypto...);
        write_packet<std::tuple<Crypto...>, PacketFormatTag<Fmts...>, Args...>::write (crypto..., packet, args...);
      }
    };


    template<typename Crypto, typename Fmts, typename ...Args>
    struct write_packet_bits;

    template<typename ...Crypto, typename Fmts, typename ...Args>
    struct write_packet_bits<std::tuple<Crypto...>, Fmts, Args...>
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

        template<std::size_t Offset, typename MessageFormat>
        static void write (MessageFormat &packet, Args const &...args)
        {
          static_assert (Offset % 8 == 0, "Bitfields do not end on a byte boundary");
          write_packet<std::tuple<Crypto...>, Fmts, Args...>::write (packet, args...);
        }
      };

      template<typename IntegralType, IntegralType Value, std::size_t BitSize, typename ...Bitfield>
      struct inner<
        bitfield::member<std::integral_constant<IntegralType, Value>, BitSize>, Bitfield...
      >
      {
        template<std::size_t Offset, typename MessageFormat>
        static void write (MessageFormat &packet, Args const &...args)
        {
          packet << Value;
          packet.shift_left (Offset, BitSize);
          inner<Bitfield...>::
            template write<Offset + BitSize> (packet, args...);
        }
      };
    };


    template<typename ...Crypto, typename Fmts, typename Field, typename ...Args>
    struct write_packet_bits<std::tuple<Crypto...>, Fmts, Field, Args...>
    {
      template<typename ...Bitfield>
      struct inner
      {
        static_assert (sizeof... (Bitfield) == 0,
                       "Base case for inner bitfield template must be empty.");

        template<std::size_t Offset, typename MessageFormat>
        static void write (MessageFormat &packet, Field const &field, Args const &...args)
        {
          static_assert (Offset % 8 == 0, "Bitfields do not end on a byte boundary");
          write_packet<std::tuple<Crypto...>, Fmts, Field, Args...>::write (packet, field, args...);
        }
      };

      template<typename IntegralType, IntegralType Value, std::size_t BitSize, typename ...Bitfield>
      struct inner<
        bitfield::member<std::integral_constant<IntegralType, Value>, BitSize>, Bitfield...
      >
      {
        template<std::size_t Offset, typename MessageFormat>
        static void write (MessageFormat &packet, Field const &field, Args const &...args)
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
        template<std::size_t Offset, typename MessageFormat>
        static void write (MessageFormat &packet, Field const &field, Args const &...args)
        {
          write_packet<std::tuple<Crypto...>, PacketFormatTag<MemberType>, Field>::write (packet, field);
          packet.shift_left (Offset, BitSize);
          write_packet_bits<std::tuple<Crypto...>, Fmts, Args...>::
            template inner<Bitfield...>::
            template write<Offset + BitSize> (packet, args...);
        }
      };
    };

    template<typename ...Crypto, typename ...Members, typename ...Fmts, typename ...Args>
    struct write_packet<std::tuple<Crypto...>, PacketFormatTag<bitfield::type<Members...>, Fmts...>, Args...>
    {
      template<typename MessageFormat>
      static void write (MessageFormat &packet, Args const &...args)
      {
        write_packet_bits<std::tuple<Crypto...>, PacketFormatTag<Fmts...>, Args...>::
          template inner<Members...>::
          template write<0> (packet, args...);
      }
    };


    template<typename ...Crypto, typename ...Members, typename ...Fmts, typename ...Fields, typename ...Args>
    struct write_packet<std::tuple<Crypto...>, PacketFormatTag<bitfield::type<Members...>, Fmts...>, std::tuple<Fields...>, Args...>
    {
      template<typename MessageFormat, std::size_t ...S>
      static void write_bitfields (seq<S...>, MessageFormat &packet, std::tuple<Fields...> const &fields, Args const &...args)
      {
        write_packet_bits<std::tuple<Crypto...>, PacketFormatTag<Fmts...>, Fields..., Args...>::
          template inner<Members...>::
          template write<0> (packet, std::get<S> (fields)..., args...);
      }

      template<typename MessageFormat>
      static void write (MessageFormat &packet, std::tuple<Fields...> const &fields, Args const &...args)
      {
        write_bitfields (make_seq<sizeof... (Fields)> (), packet, fields, args...);
      }
    };


    template<typename ...Crypto>
    struct write_packet_recurse<std::tuple<Crypto...>, PacketFormatTag<>>
    {
      template<typename MessageFormat>
      static void write (Crypto const &.../*crypto*/, MessageFormat &/*packet*/) { }
    };

    template<typename ...Crypto, typename FirstFmt, typename ...Fmts, typename FirstArg, typename ...Args>
    struct write_packet_recurse<std::tuple<Crypto...>, PacketFormatTag<FirstFmt, Fmts...>, FirstArg, Args...>
    {
      template<typename MessageFormat>
      static void write (Crypto const &...crypto, MessageFormat &packet, FirstArg const &arg, Args const &...args)
      {
        packet << arg;
        write_packet<std::tuple<Crypto...>, PacketFormatTag<Fmts...>, Args...>::write (crypto..., packet, args...);
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
        write_packet<std::tuple<>, Format, Args...>::write (packet, contents...);
      }
    };
  }
}
