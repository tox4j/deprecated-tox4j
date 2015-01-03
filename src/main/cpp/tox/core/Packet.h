#pragma once

#include "CryptoBox.h"
#include "variant.h"


template<typename T>
struct vector { };


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
    template<typename ...Contents>
    struct type
    { };

    template<typename MemberType, std::size_t Size>
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
  }

  namespace detail
  {
    template<typename Fmts, typename... Args>
    struct write_packet;

    template<typename Fmts, typename... Args>
    struct write_packet_plain;


    template<typename... Fmts, typename... Args>
    struct write_packet<PacketFormatTag<Fmts...>, Args...>
    {
      static void write (CipherText &packet, Args const &...args)
      {
        write_packet_plain<PacketFormatTag<Fmts...>, Args...>::write (packet, args...);
      }

      static void write (PlainText &plain, Args const &...args)
      {
        write_packet_plain<PacketFormatTag<Fmts...>, Args...>::write (plain, args...);
      }
    };

    template<typename IntegralType, IntegralType Value, typename... Fmts, typename... Args>
    struct write_packet<PacketFormatTag<std::integral_constant<IntegralType, Value>, Fmts...>, Args...>
    {
      static void write (CipherText &packet, Args const &...args)
      {
        packet << std::integral_constant<IntegralType, Value>::value;
        write_packet<PacketFormatTag<Fmts...>, Args...>::write (packet, args...);
      }
    };

    template<typename... Fmts, typename... Args>
    struct write_packet<PacketFormatTag<encrypted<Fmts...>>, CryptoBox, Nonce, Args...>
    {
      static void write (CipherText &packet, CryptoBox const &box, Nonce const &nonce, Args const &...args)
      {
        PlainText plain;
        write_packet_plain<PacketFormatTag<Fmts...>, Args...>::write (plain, args...);
        packet << box.encrypt (plain, nonce);
      }
    };

#if 0
    template<typename SizeType, typename... Fields, typename... Fmts, typename... Args>
    struct write_packet<PacketFormatTag<repeated<SizeType, Fields...>, Fmts...>, Args...>
    {
      static void write (CipherText &packet, Args const &...args)
      {
      }
    };
#endif


    template<>
    struct write_packet_plain<PacketFormatTag<>>
    {
      static void write (PlainText &/*plain*/)
      {
      }
    };

    template<typename FirstFmt, typename... Fmts, typename FirstArg, typename... Args>
    struct write_packet_plain<PacketFormatTag<FirstFmt, Fmts...>, FirstArg, Args...>
    {
      static void write (CipherText &packet, FirstArg const &arg, Args const &...args)
      {
        packet << arg;
        write_packet<PacketFormatTag<Fmts...>, Args...>::write (packet, args...);
      }

      static void write (PlainText &plain, FirstArg const &arg, Args const &...args)
      {
        plain << arg;
        write_packet<PacketFormatTag<Fmts...>, Args...>::write (plain, args...);
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
        vector<
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
            PacketFormatTag<Choices...>
          >::type
        >,
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
