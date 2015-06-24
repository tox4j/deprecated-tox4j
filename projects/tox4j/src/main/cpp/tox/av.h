#pragma once

#include "tox/common.h"
#include <tox/toxav.h>

#ifdef TOXAV_VERSION_MAJOR
namespace tox
{
  struct av_deleter
  {
    void operator () (ToxAV *toxav)
    {
      toxav_kill (toxav);
    }
  };

  typedef std::unique_ptr<ToxAV, av_deleter> av_ptr;

#define CALLBACK(NAME)  using callback_##NAME = detail::cb<ToxAV, toxav_##NAME##_cb, toxav_callback_##NAME>
  CALLBACK (call);
  CALLBACK (call_state);
  CALLBACK (audio_bit_rate_status);
  CALLBACK (video_bit_rate_status);
  CALLBACK (audio_receive_frame);
  CALLBACK (video_receive_frame);
#undef CALLBACK
}
#endif
