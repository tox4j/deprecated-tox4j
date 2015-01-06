#pragma once

#include "Packet.h"

#include <limits>
#include <memory>


namespace tox
{
  struct PacketDispatcher;

  struct PacketHandlerBase
  {
    friend struct PacketDispatcher;
    virtual ~PacketHandlerBase ();

  private:
    virtual Partial<void> handle_packet (CipherText &&packet, CryptoBox const &crypto) = 0;
  };


  template<typename Derived, typename PacketType, typename Decoded = typename Packet<PacketType>::Decoded>
  struct PacketHandler;

  template<typename Derived, PacketKind Kind, typename ...Contents, typename ...Args>
  struct PacketHandler<Derived, PacketFormat<Kind, Contents...>, std::tuple<Args...>>
    : PacketHandlerBase
  {
    static PacketKind const packet_kind = Kind;
    typedef PacketFormat<Kind, Contents...> packet_format;

  private:
    Partial<void> handle_packet (CipherText &&data, CryptoBox const &crypto) override
    {
      return Packet<packet_format>::decode_packet (std::move (data), crypto)
        ->* [this] (Args &&...args) {
          return static_cast<Derived *> (this)->handle (std::move (args)...);
        };
    }
  };


  struct PacketDispatcher
  {
    template<typename T, typename ...Args>
    void register_handler (Args const &...args)
    {
      register_handler (T::packet_kind, std::unique_ptr<T> (new T (args...)));
    }

    void handle (CipherText &&packet, CryptoBox const &crypto) const
    {
      assert (!packet.empty ());
      auto const &handler = handlers[packet.data ()[0]];
      if (handler != nullptr)
        handler->handle_packet (std::move (packet), crypto);
    }

  private:
    void register_handler (PacketKind kind, std::unique_ptr<PacketHandlerBase> &&handler)
    {
      handlers[static_cast<byte> (kind)] = std::move (handler);
    }

    std::array<std::unique_ptr<PacketHandlerBase>, std::numeric_limits<byte>::max ()> handlers;
  };
}
