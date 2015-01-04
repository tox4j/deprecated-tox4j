#pragma once

#include "tox/core/CryptoBox.h"
#include "tox/core/Message.h"

#include "tox/core/tuple_util.h"
#include "tox/core/variant.h"

#include "Format.h"
#include "arguments.h"


namespace tox
{
  namespace detail
  {
    template<typename DecoderArgsTuple, typename EncoderArgsTuple>
    struct packet_decoder_args;

    template<typename ...DecoderArgs, typename Arg, typename ...EncoderArgs>
    struct packet_decoder_args<std::tuple<DecoderArgs...>, std::tuple<Arg, EncoderArgs...>>
      : packet_decoder_args<std::tuple<DecoderArgs..., Arg>, std::tuple<EncoderArgs...>>
    {
    };

    template<typename ...DecoderArgs, typename ...EncoderArgs>
    struct packet_decoder_args<std::tuple<DecoderArgs...>, std::tuple<CryptoBox, Nonce, EncoderArgs...>>
      : packet_decoder_args<std::tuple<DecoderArgs...>, std::tuple<EncoderArgs...>>
    {
      typedef std::tuple<CryptoBox, Nonce> crypto_args;
    };

    template<typename ...DecoderArgs>
    struct packet_decoder_args<std::tuple<DecoderArgs...>, std::tuple<>>
    {
      typedef std::tuple<> crypto_args;
      typedef std::tuple<DecoderArgs...> decoder_args;
    };
  }


  namespace detail
  {
    template<std::size_t Member, typename Format, typename Crypto, typename ...DecoderArgs>
    struct read_packet;

    template<std::size_t Member, typename Fmt, typename ...Fmts, typename Crypto, typename ...DecoderArgs>
    struct read_packet<Member, PacketFormatTag<Fmt, Fmts...>, Crypto, DecoderArgs...>
    {
      static Partial<void> read (std::tuple<DecoderArgs...> &decoded,
                                 BitStream<PlainText> packet)
      {
        BitStream<PlainText> rest = packet >> std::get<Member> (decoded);
        return read_packet<Member + 1, PacketFormatTag<Fmts...>, Crypto, DecoderArgs...>::
          read (decoded, rest);
      }

      static Partial<void> read (Crypto const &crypto,
                                 std::tuple<DecoderArgs...> &decoded,
                                 BitStream<CipherText> packet)
      {
        BitStream<CipherText> rest = packet >> std::get<Member> (decoded);
        return read_packet<Member + 1, PacketFormatTag<Fmts...>, Crypto, DecoderArgs...>::
          read (crypto, decoded, rest);
      }
    };

    template<std::size_t Member, typename IntegralType, IntegralType Value, typename ...Fmts, typename Crypto, typename ...DecoderArgs>
    struct read_packet<Member, PacketFormatTag<std::integral_constant<IntegralType, Value>, Fmts...>, Crypto, DecoderArgs...>
    {
      static Partial<void> read (Crypto const &crypto,
                                 std::tuple<DecoderArgs...> &decoded,
                                 BitStream<CipherText> packet)
      {
        IntegralType value;
        BitStream<CipherText> rest = packet >> value;
        if (value != Value)
          return failure (Status::FormatError);
        return read_packet<Member, PacketFormatTag<Fmts...>, Crypto, DecoderArgs...>::
          read (crypto, decoded, rest);
      }
    };

    template<std::size_t Member, typename ...Fmts, typename Crypto, typename ...DecoderArgs>
    struct read_packet<Member, PacketFormatTag<encrypted<Fmts...>>, Crypto, DecoderArgs...>
    {
      static Partial<void> read (Crypto const &crypto,
                                 std::tuple<DecoderArgs...> &decoded,
                                 BitStream<CipherText> packet)
      {
        static_assert (std::tuple_size<Crypto>::value == 2,
                       "Invalid Crypto object");
        CryptoBox const &box = std::get<0> (crypto);
        Nonce const &nonce   = std::get<1> (crypto);

        return box.decrypt (packet, nonce) >>= [&](PlainText const &plain) {
          return read_packet<Member, PacketFormatTag<Fmts...>, Crypto, DecoderArgs...>::
            read (decoded, BitStream<PlainText> (plain));
        };
      }
    };

    template<std::size_t Member, typename ...Bitfields, typename ...Fmts, typename Crypto, typename ...DecoderArgs>
    struct read_packet<Member, PacketFormatTag<bitfield::type<Bitfields...>, Fmts...>, Crypto, DecoderArgs...>
    {
      static Partial<void> read (std::tuple<DecoderArgs...> &decoded,
                                 BitStream<PlainText> packet)
      {
        return read_packet<Member, PacketFormatTag<Bitfields..., Fmts...>, Crypto, DecoderArgs...>::
          read (decoded, packet);
      }
    };

    template<std::size_t Member, typename MemberType, std::size_t BitSize, typename ...Fmts, typename Crypto, typename ...DecoderArgs>
    struct read_packet<Member, PacketFormatTag<bitfield::member<MemberType, BitSize>, Fmts...>, Crypto, DecoderArgs...>
    {
      static Partial<void> read (std::tuple<DecoderArgs...> &decoded,
                                 BitStream<PlainText> packet)
      {
        BitStream<PlainText> rest = packet.bit_size<BitSize> () >> std::get<Member> (decoded);
        return read_packet<Member + 1, PacketFormatTag<Fmts...>, Crypto, DecoderArgs...>::
          read (decoded, rest);
      }
    };

    template<std::size_t Member, typename IntegralType, IntegralType Value, std::size_t BitSize, typename ...Fmts, typename Crypto, typename ...DecoderArgs>
    struct read_packet<Member, PacketFormatTag<bitfield::member<std::integral_constant<IntegralType, Value>, BitSize>, Fmts...>, Crypto, DecoderArgs...>
    {
      static Partial<void> read (std::tuple<DecoderArgs...> &decoded,
                                 BitStream<PlainText> packet)
      {
        IntegralType value;
        BitStream<PlainText> rest = packet.bit_size<BitSize> () >> value;
        if (value != Value)
          return failure (Status::FormatError);
        return read_packet<Member, PacketFormatTag<Fmts...>, Crypto, DecoderArgs...>::
          read (decoded, rest);
      }
    };

    template<std::size_t Member, typename Crypto, typename ...DecoderArgs>
    struct read_packet<Member, PacketFormatTag<>, Crypto, DecoderArgs...>
    {
      static Partial<void> read (std::tuple<DecoderArgs...> &/*decoded*/,
                                 BitStream<PlainText> /*packet*/)
      {
        return success ();
      }

      static Partial<void> read (Crypto const &/*crypto*/,
                                 std::tuple<DecoderArgs...> &/*decoded*/,
                                 BitStream<CipherText> /*packet*/)
      {
        return success ();
      }
    };
  }


  namespace detail
  {
    template<typename Format, typename Crypto, typename DecoderArgsTuple>
    struct packet_decoder_crypto;

    template<typename Format, typename ...CryptoArgs, typename ...DecoderArgs>
    struct packet_decoder_crypto<Format, std::tuple<CryptoArgs...>, std::tuple<DecoderArgs...>>
    {
      typedef std::tuple<CryptoArgs const &...> crypto_type;

      struct decoder
      {
        template<std::size_t ...S, typename Handler>
        static typename std::result_of<Handler (DecoderArgs...)>::type
        apply (seq<S...>, Handler const &handler, std::tuple<DecoderArgs...> &&args)
        {
          return handler (std::move (std::get<S> (args))...);
        }

        template<typename Handler>
        typename std::result_of<Handler (DecoderArgs...)>::type
        operator >>= (Handler const &handler)
        {
          std::tuple<DecoderArgs...> decoded;
          return read_packet<0, Format, crypto_type, DecoderArgs...>::read (crypto_, decoded, packet_) >>=
            [&] {
              return apply (make_seq<sizeof... (DecoderArgs)> (), handler, std::move (decoded));
            };
        }

        decoder (CipherText &packet, CryptoArgs const &...crypto)
          : packet_ (packet)
          , crypto_ (crypto...)
        {
        }

      private:
        BitStream<CipherText> const packet_;
        crypto_type const crypto_;
      };

      static decoder decode (CipherText &packet, CryptoArgs const &...crypto)
      {
        return decoder (packet, crypto...);
      }
    };


    template<typename Format, typename EncoderArgsTuple>
    struct packet_decoder;

    template<typename Format, typename ...EncoderArgs>
    struct packet_decoder<Format, std::tuple<EncoderArgs...>>
      : packet_decoder_crypto<
          Format,
          typename packet_decoder_args<std::tuple<>, std::tuple<EncoderArgs...>>::crypto_args,
          typename packet_decoder_args<std::tuple<>, std::tuple<EncoderArgs...>>::decoder_args
        >
    {
    };
  }
}
