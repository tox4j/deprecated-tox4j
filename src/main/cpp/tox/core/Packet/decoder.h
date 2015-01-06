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

    template<std::size_t Member, typename Fmt, typename ...Fmts, typename ...Crypto, typename ...DecodedArgs>
    struct read_packet<Member, PacketFormatTag<Fmt, Fmts...>, std::tuple<Crypto...>, DecodedArgs...>
    {
      template<typename MessageFormat>
      static Partial<BitStream<MessageFormat>> read (Crypto const &...crypto,
                                                     std::tuple<DecodedArgs...> &decoded,
                                                     BitStream<MessageFormat> packet)
      {
        auto rest = packet >> std::get<Member> (decoded);
        return read_packet<Member + 1, PacketFormatTag<Fmts...>, std::tuple<Crypto...>, DecodedArgs...>::
          read (crypto..., decoded, rest);
      }
    };

    // Flatten nested PacketFormatTags.
    template<std::size_t Member, typename ...InnerFmts, typename ...Fmts, typename ...Crypto, typename ...DecodedArgs>
    struct read_packet<Member, PacketFormatTag<PacketFormatTag<InnerFmts...>, Fmts...>, std::tuple<Crypto...>, DecodedArgs...>
    {
      template<typename MessageFormat>
      static Partial<BitStream<MessageFormat>> read (Crypto const &...crypto,
                                                     std::tuple<DecodedArgs...> &decoded,
                                                     BitStream<MessageFormat> packet)
      {
        return read_packet<Member, PacketFormatTag<InnerFmts..., Fmts...>, std::tuple<Crypto...>, DecodedArgs...>::
          read (crypto..., decoded, packet);
      }
    };

    template<std::size_t Member, typename IntegralType, IntegralType Value, typename ...Fmts, typename ...Crypto, typename ...DecodedArgs>
    struct read_packet<Member, PacketFormatTag<std::integral_constant<IntegralType, Value>, Fmts...>, std::tuple<Crypto...>, DecodedArgs...>
    {
      template<typename MessageFormat>
      static Partial<BitStream<MessageFormat>> read (Crypto const &...crypto,
                                                     std::tuple<DecodedArgs...> &decoded,
                                                     BitStream<MessageFormat> packet)
      {
        IntegralType value;
        auto rest = packet >> value;
        if (value != Value)
          return failure (Status::FormatError);
        return read_packet<Member, PacketFormatTag<Fmts...>, std::tuple<Crypto...>, DecodedArgs...>::
          read (crypto..., decoded, rest);
      }
    };

    template<std::size_t Member, typename ...Fmts, typename ...Crypto, typename ...DecodedArgs>
    struct read_packet<Member, PacketFormatTag<Nonce, Fmts...>, std::tuple<Crypto...>, DecodedArgs...>
    {
      template<typename MessageFormat>
      static Partial<BitStream<MessageFormat>> read (Crypto const &...crypto,
                                                     std::tuple<DecodedArgs...> &decoded,
                                                     BitStream<MessageFormat> packet)
      {
        Nonce &nonce = std::get<Member> (decoded);
        auto rest = packet >> nonce;
        return read_packet<Member + 1, PacketFormatTag<Fmts...>, std::tuple<Crypto..., Nonce const &>, DecodedArgs...>::
          read (crypto..., nonce, decoded, rest);
      }
    };

    template<std::size_t Member, typename ...Fmts, typename ...Crypto, typename ...DecodedArgs>
    struct read_packet<Member, PacketFormatTag<encrypted<Fmts...>>, std::tuple<Crypto...>, DecodedArgs...>
    {
      // Expected crypto args.
      typedef std::tuple<CryptoBox const &, Nonce const &> crypto_args;
      static_assert (std::is_same<std::tuple<Crypto...>, crypto_args>::value,
                     "Unexpected crypto arguments");

      static Partial<BitStream<CipherText>> read (CryptoBox const &box,
                                                  Nonce const &nonce,
                                                  std::tuple<DecodedArgs...> &decoded,
                                                  BitStream<CipherText> packet)
      {
        CipherText encrypted;
        auto rest = packet >> encrypted;

        return box.decrypt (encrypted, nonce)

          ->* [&](PlainText const &plain) {
            return read_packet<Member, PacketFormatTag<Fmts...>, std::tuple<>, DecodedArgs...>::
              read (decoded, BitStream<PlainText> (plain));
          }

          ->* [&] {
            return success (std::move (rest));
          };
      }
    };

    template<std::size_t Member, typename SizeType, typename ...Fields, typename ...Fmts, typename ...Crypto, typename ...DecodedArgs>
    struct read_packet<Member, PacketFormatTag<repeated<SizeType, Fields...>, Fmts...>, std::tuple<Crypto...>, DecodedArgs...>
    {
      typedef typename std::tuple_element<Member, std::tuple<DecodedArgs...>>::type::value_type record_type;

      template<typename MessageFormat, typename ...FieldTypes>
      static Partial<BitStream<MessageFormat>> read (Crypto const &...crypto,
                                                     std::tuple<FieldTypes...> &decoded_field,
                                                     BitStream<MessageFormat> packet)
      {
        return read_packet<0, PacketFormatTag<Fields...>, std::tuple<Crypto...>, FieldTypes...>::
          read (crypto..., decoded_field, packet);
      }

      template<typename MessageFormat>
      static Partial<BitStream<MessageFormat>> read (Crypto const &...crypto,
                                                     std::tuple<DecodedArgs...> &decoded,
                                                     BitStream<MessageFormat> packet)
      {
        SizeType size;
        BitStream<PlainText> rest = packet >> size;

        std::get<Member> (decoded).reserve (size);

        for (SizeType i = 0; i < size; i++)
          {
            record_type record;

            auto result = read (crypto..., record, rest)

              ->* [&](BitStream<PlainText> const &new_rest) {
                std::get<Member> (decoded).push_back (std::move (record));

                rest.~BitStream<PlainText> ();
                renew (rest, new_rest);
                return success ();
              };

            if (!result.ok ())
              return failure (result.code ());
          }

        return read_packet<Member + 1, PacketFormatTag<Fmts...>, std::tuple<Crypto...>, DecodedArgs...>::
          read (crypto..., decoded, rest);
      }
    };

    template<std::size_t Member, typename ...Choices, typename ...Fmts, typename ...Crypto, typename ...DecodedArgs>
    struct read_packet<Member, PacketFormatTag<choice<Choices...>, Fmts...>, std::tuple<Crypto...>, DecodedArgs...>
    {
      template<typename FormatChoice, typename MessageFormat, typename ...Types>
      static Partial<BitStream<MessageFormat>> try_read (Crypto const &...crypto,
                                                         std::tuple<Types...> &decoded,
                                                         BitStream<MessageFormat> packet)
      {
        return read_packet<0, FormatChoice, std::tuple<Crypto...>, Types...>::
          read (crypto..., decoded, packet);
      }

      template<std::size_t Choice, typename MessageFormat>
      struct read_variant
      {
        template<typename ...VariantTypes>
        static Partial<BitStream<MessageFormat>> read (Crypto const &...crypto,
                                                       variant<VariantTypes...> &decoded_variant,
                                                       BitStream<MessageFormat> packet)
        {
          (void) decoded_variant;
          typedef typename std::tuple_element<Choice - 1, std::tuple<Choices...>>::type format_choice;
          typedef typename variant_type<Choice - 1, variant<VariantTypes...>>::type variant_choice;

          variant_choice decoded;
          auto result = try_read<format_choice> (crypto..., decoded, packet);
          if (result.ok ())
            {
              decoded_variant = decoded;
              return result;
            }

          return read_variant<Choice - 1, MessageFormat>::read (crypto..., decoded_variant, packet);
        }
      };

      template<typename MessageFormat>
      struct read_variant<0, MessageFormat>
      {
        template<typename ...VariantTypes>
        static Partial<BitStream<MessageFormat>> read (Crypto const &.../*crypto*/,
                                                       variant<VariantTypes...> &/*decoded_variant*/,
                                                       BitStream<MessageFormat> /*packet*/)
        {
          return failure (Status::FormatError);
        }
      };

      template<typename MessageFormat>
      static Partial<BitStream<MessageFormat>> read (Crypto const &...crypto,
                                                     std::tuple<DecodedArgs...> &decoded,
                                                     BitStream<MessageFormat> packet)
      {
        return read_variant<sizeof... (Choices), MessageFormat>::read (crypto..., std::get<Member> (decoded), packet)

          ->* [&](BitStream<MessageFormat> const &rest) {
            return read_packet<Member + 1, PacketFormatTag<Fmts...>, std::tuple<Crypto...>, DecodedArgs...>::
              read (crypto..., decoded, rest);
          };
      }
    };

    template<std::size_t Member, typename ...Bitfields, typename ...Fmts, typename ...Crypto, typename ...DecodedArgs>
    struct read_packet<Member, PacketFormatTag<bitfield::type<Bitfields...>, Fmts...>, std::tuple<Crypto...>, DecodedArgs...>
    {
      template<typename MessageFormat>
      static Partial<BitStream<MessageFormat>> read (Crypto const &...crypto,
                                                     std::tuple<DecodedArgs...> &decoded,
                                                     BitStream<MessageFormat> packet)
      {
        return read_packet<Member, PacketFormatTag<Bitfields..., Fmts...>, std::tuple<Crypto...>, DecodedArgs...>::
          read (crypto..., decoded, packet);
      }
    };

    template<std::size_t Member, typename MemberType, std::size_t BitSize, typename ...Fmts, typename ...Crypto, typename ...DecodedArgs>
    struct read_packet<Member, PacketFormatTag<bitfield::member<MemberType, BitSize>, Fmts...>, std::tuple<Crypto...>, DecodedArgs...>
    {
      template<typename MessageFormat>
      static Partial<BitStream<MessageFormat>> read (Crypto const &...crypto,
                                                     std::tuple<DecodedArgs...> &decoded,
                                                     BitStream<MessageFormat> packet)
      {
        auto rest = packet.template bit_size<BitSize> () >> std::get<Member> (decoded);
        return read_packet<Member + 1, PacketFormatTag<Fmts...>, std::tuple<Crypto...>, DecodedArgs...>::
          read (crypto..., decoded, rest);
      }
    };

    template<std::size_t Member, typename IntegralType, IntegralType Value, std::size_t BitSize, typename ...Fmts, typename ...Crypto, typename ...DecodedArgs>
    struct read_packet<Member, PacketFormatTag<bitfield::member<std::integral_constant<IntegralType, Value>, BitSize>, Fmts...>, std::tuple<Crypto...>, DecodedArgs...>
    {
      template<typename MessageFormat>
      static Partial<BitStream<MessageFormat>> read (Crypto const &...crypto,
                                                     std::tuple<DecodedArgs...> &decoded,
                                                     BitStream<MessageFormat> packet)
      {
        IntegralType value;
        auto rest = packet.template bit_size<BitSize> () >> value;
        if (value != Value)
          return failure (Status::FormatError);
        return read_packet<Member, PacketFormatTag<Fmts...>, std::tuple<Crypto...>, DecodedArgs...>::
          read (crypto..., decoded, rest);
      }
    };

    template<std::size_t Member, typename ...Crypto, typename ...DecodedArgs>
    struct read_packet<Member, PacketFormatTag<>, std::tuple<Crypto...>, DecodedArgs...>
    {
      template<typename MessageFormat>
      static Partial<BitStream<MessageFormat>> read (Crypto const &.../*crypto*/,
                                                     std::tuple<DecodedArgs...> &/*decoded*/,
                                                     BitStream<MessageFormat> packet)
      {
        return success (std::move (packet));
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

        template<std::size_t ...S, typename Handler>
        typename std::result_of<Handler (DecodedArgs...)>::type
        read (seq<S...>, Handler const &handler)
        {
          Decoded decoded;
          return read_packet<0, Format, Crypto, DecodedArgs...>::
            read (std::get<S> (crypto_)..., decoded, BitStream<CipherText> (packet_))
              ->* [&] {
                return apply (make_seq<sizeof... (DecodedArgs)> (), handler, std::move (decoded));
              };
        }

        template<typename Handler>
        typename std::result_of<Handler (DecodedArgs...)>::type
        operator ->* (Handler const &handler)
        {
          return read (make_seq<sizeof... (CryptoArgs)> (), handler);
        }

        decoder (CipherText &&packet, CryptoArgs const &...crypto)
          : packet_ (packet)
          , crypto_ (crypto...)
        {
        }

      private:
        CipherText const packet_;
        Crypto const crypto_;
      };

      static decoder decode_packet (CipherText &&packet, CryptoArgs const &...crypto)
      {
        return decoder (std::move (packet), crypto...);
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
