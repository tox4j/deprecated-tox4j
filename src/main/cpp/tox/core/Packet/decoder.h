#pragma once

#include "tox/core/CryptoBox.h"
#include "tox/core/KeyPair.h"
#include "tox/core/Message.h"
#include "tox/core/Nonce.h"

#include "tox/core/tuple_util.h"
#include "tox/core/variant.h"

#include "Format.h"
#include "arguments.h"


namespace tox
{
  namespace detail
  {
    template<typename DecodedArgsTuple, typename EncoderArgsTuple>
    struct packet_decoder_args;

    template<typename ...DecodedArgs, typename Arg, typename ...EncoderArgs>
    struct packet_decoder_args<std::tuple<DecodedArgs...>, std::tuple<Arg, EncoderArgs...>>
      : packet_decoder_args<std::tuple<DecodedArgs..., Arg>, std::tuple<EncoderArgs...>>
    {
    };

    template<typename ...DecodedArgs, typename ...EncoderArgs>
    struct packet_decoder_args<std::tuple<DecodedArgs...>, std::tuple<CryptoBox, EncoderArgs...>>
      : packet_decoder_args<std::tuple<DecodedArgs...>, std::tuple<EncoderArgs...>>
    {
      typedef std::tuple<CryptoBox const &> crypto_args;
    };

    template<typename ...DecodedArgs>
    struct packet_decoder_args<std::tuple<DecodedArgs...>, std::tuple<>>
    {
      typedef std::tuple<> crypto_args;
      typedef std::tuple<DecodedArgs...> decoder_args;
    };
  }


  namespace detail
  {
    template<std::size_t Member, typename Format, typename Crypto, typename ...DecodedArgs>
    struct read_packet;

    template<std::size_t Member, typename Fmt, typename ...Fmts, typename Crypto, typename ...DecodedArgs>
    struct read_packet<Member, PacketFormatTag<Fmt, Fmts...>, Crypto, DecodedArgs...>
    {
      static Partial<BitStream<PlainText>> read (std::tuple<DecodedArgs...> &decoded,
                                                 BitStream<PlainText> packet)
      {
        auto rest = packet >> std::get<Member> (decoded);
        return read_packet<Member + 1, PacketFormatTag<Fmts...>, Crypto, DecodedArgs...>::
          read (decoded, rest);
      }

      static Partial<BitStream<CipherText>> read (Crypto const &crypto,
                                                  std::tuple<DecodedArgs...> &decoded,
                                                  BitStream<CipherText> packet)
      {
        auto rest = packet >> std::get<Member> (decoded);
        return read_packet<Member + 1, PacketFormatTag<Fmts...>, Crypto, DecodedArgs...>::
          read (crypto, decoded, rest);
      }
    };

    template<std::size_t Member, typename IntegralType, IntegralType Value, typename ...Fmts, typename Crypto, typename ...DecodedArgs>
    struct read_packet<Member, PacketFormatTag<std::integral_constant<IntegralType, Value>, Fmts...>, Crypto, DecodedArgs...>
    {
      static Partial<BitStream<CipherText>> read (Crypto const &crypto,
                                                  std::tuple<DecodedArgs...> &decoded,
                                                  BitStream<CipherText> packet)
      {
        IntegralType value;
        auto rest = packet >> value;
        if (value != Value)
          return failure (Status::FormatError);
        return read_packet<Member, PacketFormatTag<Fmts...>, Crypto, DecodedArgs...>::
          read (crypto, decoded, rest);
      }
    };

    template<std::size_t Member, typename ...Fmts, typename ...CryptoArgs, typename ...DecodedArgs>
    struct read_packet<Member, PacketFormatTag<Nonce, Fmts...>, std::tuple<CryptoArgs...>, DecodedArgs...>
    {
      static Partial<BitStream<CipherText>> read (std::tuple<CryptoArgs...> const &crypto,
                                                  std::tuple<DecodedArgs...> &decoded,
                                                  BitStream<CipherText> packet)
      {
        Nonce &nonce = std::get<Member> (decoded);
        auto rest = packet >> nonce;
        return read_packet<Member + 1, PacketFormatTag<Fmts...>, std::tuple<CryptoArgs..., Nonce const &>, DecodedArgs...>::
          read (std::tuple_cat (crypto, std::tuple<Nonce const &> (nonce)), decoded, rest);
      }

      static Partial<BitStream<PlainText>> read (std::tuple<DecodedArgs...> &decoded,
                                                 BitStream<PlainText> packet)
      {
        auto rest = packet >> std::get<Member> (decoded);
        return read_packet<Member + 1, PacketFormatTag<Fmts...>, std::tuple<CryptoArgs...>, DecodedArgs...>::
          read (decoded, rest);
      }
    };

    template<std::size_t Member, typename ...Fmts, typename Crypto, typename ...DecodedArgs>
    struct read_packet<Member, PacketFormatTag<encrypted<Fmts...>>, Crypto, DecodedArgs...>
    {
      // Expected crypto args.
      typedef std::tuple<CryptoBox const &, Nonce const &> crypto_args;
      static_assert (std::is_same<Crypto, crypto_args>::value,
                     "Unexpected crypto arguments");

      static Partial<BitStream<CipherText>> read (Crypto const &crypto,
                                                  std::tuple<DecodedArgs...> &decoded,
                                                  BitStream<CipherText> packet)
      {
        CryptoBox const &box = std::get<0> (crypto);
        Nonce const &nonce   = std::get<1> (crypto);

        CipherText encrypted;
        auto rest = packet >> encrypted;

        return box.decrypt (encrypted, nonce)

          >>= [&](PlainText const &plain) {
            return read_packet<Member, PacketFormatTag<Fmts...>, std::tuple<>, DecodedArgs...>::
              read (decoded, BitStream<PlainText> (plain))

          >> [&] {
            return success (rest);

        }; };
      }
    };

    template<std::size_t Member, typename SizeType, typename ...Fields, typename ...Fmts, typename Crypto, typename ...DecodedArgs>
    struct read_packet<Member, PacketFormatTag<repeated<SizeType, Fields...>, Fmts...>, Crypto, DecodedArgs...>
    {
      typedef typename std::tuple_element<Member, std::tuple<DecodedArgs...>>::type::value_type record_type;

      template<typename ...FieldTypes>
      static Partial<BitStream<PlainText>> read (BitStream<PlainText> packet,
                                                 std::tuple<FieldTypes...> &decoded_field)
      {
        return read_packet<0, PacketFormatTag<Fields...>, Crypto, FieldTypes...>::
          read (decoded_field, packet);
      }

      static Partial<BitStream<PlainText>> read (std::tuple<DecodedArgs...> &decoded,
                                                 BitStream<PlainText> packet)
      {
        SizeType size;
        BitStream<PlainText> rest = packet >> size;

        std::get<Member> (decoded).reserve (size);

        for (SizeType i = 0; i < size; i++)
          {
            record_type record;

            auto result = read (rest, record)

              >>= [&](BitStream<PlainText> const &new_rest) {
                std::get<Member> (decoded).push_back (std::move (record));

                rest.~BitStream<PlainText> ();
                renew (rest, new_rest);
                return success ();
              };

            if (!result.ok ())
              return failure (result.code ());
          }

        return read_packet<Member + 1, PacketFormatTag<Fmts...>, Crypto, DecodedArgs...>::
          read (decoded, rest);
      }
    };

#if 0
    template<std::size_t Member, typename ...Choices, typename ...Fmts, typename Crypto, typename ...DecodedArgs>
    struct read_packet<Member, PacketFormatTag<choice<Choices...>, Fmts...>, Crypto, DecodedArgs...>
    {
      static Partial<BitStream<PlainText>> read (std::tuple<DecodedArgs...> &decoded,
                                                 BitStream<PlainText> packet)
      {
        return read_packet<Member, PacketFormatTag<Bitfields..., Fmts...>, Crypto, DecodedArgs...>::
          read (decoded, packet);
      }
    };
#endif

    template<std::size_t Member, typename ...Bitfields, typename ...Fmts, typename Crypto, typename ...DecodedArgs>
    struct read_packet<Member, PacketFormatTag<bitfield::type<Bitfields...>, Fmts...>, Crypto, DecodedArgs...>
    {
      static Partial<BitStream<PlainText>> read (std::tuple<DecodedArgs...> &decoded,
                                                 BitStream<PlainText> packet)
      {
        return read_packet<Member, PacketFormatTag<Bitfields..., Fmts...>, Crypto, DecodedArgs...>::
          read (decoded, packet);
      }
    };

    template<std::size_t Member, typename MemberType, std::size_t BitSize, typename ...Fmts, typename Crypto, typename ...DecodedArgs>
    struct read_packet<Member, PacketFormatTag<bitfield::member<MemberType, BitSize>, Fmts...>, Crypto, DecodedArgs...>
    {
      static Partial<BitStream<PlainText>> read (std::tuple<DecodedArgs...> &decoded,
                                                 BitStream<PlainText> packet)
      {
        auto rest = packet.bit_size<BitSize> () >> std::get<Member> (decoded);
        return read_packet<Member + 1, PacketFormatTag<Fmts...>, Crypto, DecodedArgs...>::
          read (decoded, rest);
      }
    };

    template<std::size_t Member, typename IntegralType, IntegralType Value, std::size_t BitSize, typename ...Fmts, typename Crypto, typename ...DecodedArgs>
    struct read_packet<Member, PacketFormatTag<bitfield::member<std::integral_constant<IntegralType, Value>, BitSize>, Fmts...>, Crypto, DecodedArgs...>
    {
      static Partial<BitStream<PlainText>> read (std::tuple<DecodedArgs...> &decoded,
                                                 BitStream<PlainText> packet)
      {
        IntegralType value;
        auto rest = packet.bit_size<BitSize> () >> value;
        if (value != Value)
          return failure (Status::FormatError);
        return read_packet<Member, PacketFormatTag<Fmts...>, Crypto, DecodedArgs...>::
          read (decoded, rest);
      }
    };

    template<std::size_t Member, typename Crypto, typename ...DecodedArgs>
    struct read_packet<Member, PacketFormatTag<>, Crypto, DecodedArgs...>
    {
      static Partial<BitStream<PlainText>> read (std::tuple<DecodedArgs...> &/*decoded*/,
                                                 BitStream<PlainText> packet)
      {
        return success (packet);
      }

      static Partial<BitStream<CipherText>> read (Crypto const &/*crypto*/,
                                                  std::tuple<DecodedArgs...> &/*decoded*/,
                                                  BitStream<CipherText> packet)
      {
        return success (packet);
      }
    };
  }


  namespace detail
  {
    template<typename Format, typename Crypto, typename Decoded>
    struct packet_decoder_crypto;

    template<typename Format, typename ...CryptoArgs, typename ...DecodedArgs>
    struct packet_decoder_crypto<Format, std::tuple<CryptoArgs...>, std::tuple<DecodedArgs...>>
    {
      typedef std::tuple<CryptoArgs...> Crypto;
      typedef std::tuple<DecodedArgs...> Decoded;

      struct decoder
      {
        template<std::size_t ...S, typename Handler>
        static typename std::result_of<Handler (DecodedArgs...)>::type
        apply (seq<S...>, Handler const &handler, std::tuple<DecodedArgs...> &&args)
        {
          return handler (std::move (std::get<S> (args))...);
        }

        template<typename Handler>
        typename std::result_of<Handler (DecodedArgs...)>::type
        operator >>= (Handler const &handler)
        {
          Decoded decoded;
          return read_packet<0, Format, Crypto, DecodedArgs...>::
            read (crypto_, decoded, packet_) >>
              [&] {
                return apply (make_seq<sizeof... (DecodedArgs)> (), handler, std::move (decoded));
              };
        }

        decoder (CipherText &packet, CryptoArgs const &...crypto)
          : packet_ (packet)
          , crypto_ (crypto...)
        {
        }

      private:
        BitStream<CipherText> const packet_;
        Crypto const crypto_;
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
