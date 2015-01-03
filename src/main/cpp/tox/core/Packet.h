#pragma once

#include "CryptoBox.h"
#include "variant.h"

#include <limits>


namespace tox
{
  enum class PacketKind
    : byte
  {
    PingRequest         = 0x00,
    PingResponse        = 0x01,

    NodesRequest        = 0x02,
    NodesResponse       = 0x04,

    CookieRequest       = 0x18,
    CookieResponse      = 0x19,

    CryptoHandshake     = 0x1a,
    CryptoData          = 0x1b,
    Crypto              = 0x20,

    LANDiscovery        = 0x21,
  };


  CipherText &operator << (CipherText &packet, PacketKind kind);
  ByteStream<CipherText> operator >> (ByteStream<CipherText> const &packet, PacketKind kind);


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


  template<PacketKind Kind, typename ...Contents>
  using PacketFormat = PacketFormatTag<
    std::integral_constant<PacketKind, Kind>,
    Contents...
  >;


  namespace detail
  {
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
  }

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
      static void write (CipherText &packet, Args const &...args)
      {
        packet << std::integral_constant<IntegralType, Value>::value;
        write_packet<PacketFormatTag<Fmts...>, Args...>::write (packet, args...);
      }
    };

    template<typename ...Fmts, typename ...Args>
    struct write_packet<PacketFormatTag<encrypted<Fmts...>>, CryptoBox, Nonce, Args...>
    {
      static void write (CipherText &packet, CryptoBox const &box, Nonce const &nonce, Args const &...args)
      {
        PlainText plain;
        write_packet<PacketFormatTag<Fmts...>, Args...>::write (plain, args...);
        packet << box.encrypt (plain, nonce);
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


    template<typename ...Members, typename ...Fmts, typename ...Fields, typename ...Args>
    struct write_packet<PacketFormatTag<bitfield::type<Members...>, Fmts...>, std::tuple<Fields...>, Args...>
    {
      template<std::size_t ...S>
      static void write_bitfields (seq<S...>, PlainText &packet, std::tuple<Fields...> const &fields, Args const &...args)
      {
        //write_packet<PacketFormatTag<Members..., Fmts...>, Fields..., Args...>::
          //write (packet, std::get<S> (fields)..., args...);
      }

      static void write (PlainText &packet, std::tuple<Fields...> const &fields, Args const &...args)
      {
        write_bitfields (make_seq<sizeof... (Fields)> (), packet, fields, args...);
      }
    };


    template<>
    struct write_packet_recurse<PacketFormatTag<>>
    {
      static void write (PlainText &/*plain*/)
      {
      }
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
    };
  }


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
        Nonce,
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


    template<typename Format, typename ArgsTuple>
    struct packet_constructor;

    template<typename Format, typename ...Args>
    struct packet_constructor<Format, std::tuple<Args...>>
    {
      static void create (Args const &...contents);

      packet_constructor (Args const &...contents)
      {
        write_packet<Format, Args...>::write (packet_, contents...);
      }

      byte const *data () const { return packet_.data (); }
      std::size_t size () const { return packet_.size (); }

    private:
      CipherText packet_;
    };


    template<typename Format>
    using packet_constructor_t =
      packet_constructor<
        Format,
        typename packet_arguments<Format>::type
      >;
  }


  template<typename Format>
  struct Packet
    : detail::packet_constructor_t<Format>
  {
    using detail::packet_constructor_t<Format>::packet_constructor_t;
  };
}
