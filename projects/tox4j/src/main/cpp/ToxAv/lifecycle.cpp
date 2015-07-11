#include "ToxAv.h"
#include "../ToxCore/ToxCore.h"

#ifdef TOXAV_VERSION_MAJOR

using namespace av;


static void
tox4j_call_cb (ToxAV *av, uint32_t friend_number, bool audio_enabled, bool video_enabled, Events &events)
{
  unused (av);
  auto msg = events.add_call ();
  msg->set_friend_number (friend_number);
  msg->set_audio_enabled (audio_enabled);
  msg->set_video_enabled (video_enabled);
}


static void
tox4j_call_state_cb (ToxAV *av, uint32_t friend_number, uint32_t state, Events &events)
{
  unused (av);
  auto msg = events.add_call_state ();
  msg->set_friend_number (friend_number);

  using proto::CallState;
#define call_state_case(STATE)                  \
  if (state & TOXAV_FRIEND_CALL_STATE_##STATE)  \
    msg->add_call_state (CallState::STATE)
  call_state_case (ERROR);
  call_state_case (FINISHED);
  call_state_case (SENDING_A);
  call_state_case (SENDING_V);
  call_state_case (ACCEPTING_A);
  call_state_case (ACCEPTING_V);
#undef call_state_case
}


static void
tox4j_audio_bit_rate_status_cb (ToxAV *av,
                                uint32_t friend_number,
                                bool stable,
                                uint32_t bit_rate,
                                Events &events)
{
  unused (av);
  auto msg = events.add_audio_bit_rate_status ();
  msg->set_friend_number (friend_number);
  msg->set_stable (stable);
  msg->set_bit_rate (bit_rate);
}


static void
tox4j_video_bit_rate_status_cb (ToxAV *av,
                                uint32_t friend_number,
                                bool stable,
                                uint32_t bit_rate,
                                Events &events)
{
  unused (av);
  auto msg = events.add_video_bit_rate_status ();
  msg->set_friend_number (friend_number);
  msg->set_stable (stable);
  msg->set_bit_rate (bit_rate);
}


static void
tox4j_audio_receive_frame_cb (ToxAV *av,
                              uint32_t friend_number,
                              int16_t const *pcm,
                              size_t sample_count,
                              uint8_t channels,
                              uint32_t sampling_rate,
                              Events &events)
{
  unused (av);
  auto msg = events.add_audio_receive_frame ();
  msg->set_friend_number (friend_number);

  for (size_t i = 0; i < sample_count * channels; i++)
    msg->add_pcm (pcm[i]);

  msg->set_channels (channels);
  msg->set_sampling_rate (sampling_rate);
}


static void
tox4j_video_receive_frame_cb (ToxAV *av,
                              uint32_t friend_number,
                              uint16_t width, uint16_t height,
                              uint8_t const *y, uint8_t const *u, uint8_t const *v/*, uint8_t const *a*/,
                              int32_t ystride, int32_t ustride, int32_t vstride/*, int32_t astride*/,
                              Events &events)
{
  unused (av);
  auto msg = events.add_video_receive_frame ();
  msg->set_friend_number (friend_number);
  msg->set_width (width);
  msg->set_height (height);
  msg->set_y (y, width * height);
  msg->set_u (u, width * height);
  msg->set_v (v, width * height);
#if 0
  if (a != nullptr)
    msg->set_a (a, width * height);
#endif
  msg->set_y_stride (ystride);
  msg->set_u_stride (ustride);
  msg->set_v_stride (vstride);
#if 0
  msg->set_a_stride (astride);
#endif
}


static tox::av_ptr
toxav_new_unique (Tox *tox, TOXAV_ERR_NEW *error)
{
  return tox::av_ptr (toxav_new (tox, error));
}


/*
 * Class:     im_tox_tox4j_impl_ToxAvJni
 * Method:    toxavNew
 * Signature: (ZZILjava/lang/String;I)I
 */
TOX_METHOD (jint, New,
  jint toxInstanceNumber)
{
  register_funcs (
#define CALLBACK(NAME)   register_func (tox4j_##NAME##_cb),
#include "tox/generated/av.h"
#undef CALLBACK
    register_func (toxav_new_unique)
  );

  return core::instances.with_instance (env, toxInstanceNumber,
    [=] (Tox *tox, core::Events &)
      {
        return instances.with_error_handling (env,
          [env] (tox::av_ptr toxav)
            {
              tox4j_assert (toxav != nullptr);

              // Create the master events object and set up our callbacks.
              auto events = tox::callbacks<ToxAV> (std::unique_ptr<Events> (new Events))
#define CALLBACK(NAME)   .set<tox::callback_##NAME, tox4j_##NAME##_cb> ()
#include "tox/generated/av.h"
#undef CALLBACK
                .set (toxav.get ());

              // We can create the new instance outside instance_manager's critical section.
              // This call locks the instance manager.
              return instances.add (
                env,
                std::move (toxav),
                std::move (events)
              );
            },
          toxav_new_unique, tox
        );
      }
  );
}

/*
 * Class:     im_tox_tox4j_impl_ToxAvJni
 * Method:    toxavKill
 * Signature: (I)I
 */
TOX_METHOD (void, Kill,
  jint instanceNumber)
{
  instances.kill (env, instanceNumber);
}

/*
 * Class:     im_tox_tox4j_impl_ToxAvJni
 * Method:    toxavFinalize
 * Signature: (I)V
 */
TOX_METHOD (void, Finalize,
  jint instanceNumber)
{
  instances.finalize (env, instanceNumber);
}

#endif
