#pragma once

#include "Packet.h"
#include <limits>
#include <memory>


namespace tox
{
  struct PacketHandler
  {
    //virtual void handle (InPacket &packet);
  };

  struct PacketDispatcher
  {
    template<typename T>
    void register_handler ()
    {
      register_handler (T::packet_kind, std::make_shared<T> ());
    }

  private:
    void register_handler (PacketKind kind, std::shared_ptr<PacketHandler> handler);

    std::array<std::shared_ptr<PacketHandler>, std::numeric_limits<byte>::max ()> handlers;
  };
}
