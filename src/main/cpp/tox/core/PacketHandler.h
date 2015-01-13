#pragma once

#include "Packet.h"

#include <limits>
#include <memory>


namespace tox
{
  template<typename Result>
  struct PacketDispatcher;

  template<typename Result>
  struct PacketHandlerBase
  {
    friend struct PacketDispatcher<Result>;

    virtual ~PacketHandlerBase () { }

  private:
    virtual Partial<Result> handle_packet (CipherText &&packet, CryptoBox const &crypto) = 0;
  };


  template<typename Derived, typename PacketType, typename Result = void, typename Decoded = typename Packet<PacketType>::Decoded>
  struct PacketHandler;

  template<typename Derived, typename Result, PacketKind Kind, typename ...Contents, typename ...Args>
  struct PacketHandler<Derived, PacketFormat<Kind, Contents...>, Result, std::tuple<Args...>>
    : PacketHandlerBase<Result>
  {
    static PacketKind const packet_kind = Kind;
    typedef PacketFormat<Kind, Contents...> packet_format;

  private:
    Partial<Result> handle_packet (CipherText &&data, CryptoBox const &crypto) override
    {
      return Packet<packet_format>::decode_packet (std::move (data), crypto)
        ->* [this] (Args &&...args) {
          return static_cast<Derived *> (this)->handle (std::move (args)...);
        };
    }
  };


  template<typename Result = void>
  struct PacketDispatcher
  {
    template<typename T, typename ...Args>
    void register_handler (Args &&...args)
    {
      register_handler (T::packet_kind, std::unique_ptr<T> (new T (std::forward<Args> (args)...)));
    }

    Partial<Result> handle (CipherText &&packet, CryptoBox const &crypto) const
    {
      assert (!packet.empty ());
      auto const &handler = handlers[packet.data ()[0]];
      if (handler != nullptr)
        return handler->handle_packet (std::move (packet), crypto);
      return failure ();
    }

  private:
    void register_handler (PacketKind kind, std::unique_ptr<PacketHandlerBase<Result>> &&handler)
    {
      handlers[static_cast<byte> (kind)] = std::move (handler);
    }

    std::array<std::unique_ptr<PacketHandlerBase<Result>>, std::numeric_limits<byte>::max ()> handlers;
  };
}
