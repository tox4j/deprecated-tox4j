#pragma once

#include "tox/common.h"
#include <tox/tox.h>


namespace tox
{
  struct core_deleter
  {
    void operator () (Tox *tox)
    {
      tox_kill (tox);
    }
  };

  typedef std::unique_ptr<Tox, core_deleter> core_ptr;

#define CALLBACK(NAME)  using callback_##NAME = detail::cb<Tox, tox_##NAME##_cb, tox_callback_##NAME>
  CALLBACK (self_connection_status);
  CALLBACK (friend_name);
  CALLBACK (friend_status_message);
  CALLBACK (friend_status);
  CALLBACK (friend_connection_status);
  CALLBACK (friend_typing);
  CALLBACK (friend_read_receipt);
  CALLBACK (friend_request);
  CALLBACK (friend_message);
  CALLBACK (file_recv_control);
  CALLBACK (file_chunk_request);
  CALLBACK (file_recv);
  CALLBACK (file_recv_chunk);
  CALLBACK (friend_lossy_packet);
  CALLBACK (friend_lossless_packet);
#undef CALLBACK
}
