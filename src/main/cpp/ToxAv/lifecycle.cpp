#include "ToxAv.h"
#include "../ToxCore/ToxCore.h"

#ifdef TOXAV_VERSION_MAJOR

using namespace av;


static void
tox4j_call_cb (ToxAV *av, uint32_t friend_number, bool audio_enabled, bool video_enabled, Events &events)
{
  unused (av);
  auto msg = events.add_call ();
  msg->set_friendnumber (friend_number);
  msg->set_audioenabled (audio_enabled);
  msg->set_videoenabled (video_enabled);
}


static void
tox4j_call_state_cb (ToxAV *av, uint32_t friend_number, uint32_t state, Events &events)
{
  unused (av);
  auto msg = events.add_callstate ();
  msg->set_friendnumber (friend_number);

  using proto::CallState;
#define call_state_case(STATE)          \
  if (state & TOXAV_CALL_STATE_##STATE) \
    msg->add_state (CallState::STATE)
  call_state_case (ERROR);
  call_state_case (FINISHED);
  call_state_case (SENDING_A);
  call_state_case (SENDING_V);
  call_state_case (RECEIVING_A);
  call_state_case (RECEIVING_V);
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
  auto msg = events.add_audiobitratestatus ();
  msg->set_friendnumber (friend_number);
  msg->set_stable (stable);
  msg->set_bitrate (bit_rate);
}


static void
tox4j_video_bit_rate_status_cb (ToxAV *av,
                                uint32_t friend_number,
                                bool stable,
                                uint32_t bit_rate,
                                Events &events)
{
  unused (av);
  auto msg = events.add_videobitratestatus ();
  msg->set_friendnumber (friend_number);
  msg->set_stable (stable);
  msg->set_bitrate (bit_rate);
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
  auto msg = events.add_audioreceiveframe ();
  msg->set_friendnumber (friend_number);

  for (size_t i = 0; i < sample_count * channels; i++)
    msg->add_pcm (pcm[i]);

  msg->set_channels (channels);
  msg->set_samplingrate (sampling_rate);
}


static void
tox4j_video_receive_frame_cb (ToxAV *av,
                              uint32_t friend_number,
                              uint16_t width, uint16_t height,
                              uint8_t const *y, uint8_t const *u, uint8_t const *v, uint8_t const *a,
                              int32_t ystride, int32_t ustride, int32_t vstride, int32_t astride,
                              Events &events)
{
  unused (av);
  auto msg = events.add_videoreceiveframe ();
  msg->set_friendnumber (friend_number);
  msg->set_width (width);
  msg->set_height (height);
  msg->set_y (y, width * height);
  msg->set_u (u, width * height);
  msg->set_v (v, width * height);
  if (a != nullptr)
    msg->set_a (a, width * height);
  msg->set_ystride (ystride);
  msg->set_ustride (ustride);
  msg->set_vstride (vstride);
  msg->set_astride (astride);
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
  return core::instances.with_instance (env, toxInstanceNumber,
    [=] (Tox *tox, core::Events &)
      {
        return instances.with_error_handling (env,
          [env] (tox::av_ptr toxav)
            {
              tox4j_assert (toxav != nullptr);

              // Create the master events object and set up our callbacks.
              auto events = tox::callbacks<ToxAV> (std::unique_ptr<Events> (new Events))
                .set<tox::callback_call                 , tox4j_call_cb                 > ()
                .set<tox::callback_call_state           , tox4j_call_state_cb           > ()
                .set<tox::callback_audio_bit_rate_status, tox4j_audio_bit_rate_status_cb> ()
                .set<tox::callback_video_bit_rate_status, tox4j_video_bit_rate_status_cb> ()
                .set<tox::callback_audio_receive_frame  , tox4j_audio_receive_frame_cb  > ()
                .set<tox::callback_video_receive_frame  , tox4j_video_receive_frame_cb  > ()
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
