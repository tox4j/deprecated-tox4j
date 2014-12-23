#include <tox/av.h>
#include <tox/av_uncompat.h>

#include <tox/toxav.h>

#include "core_private.h"


struct new_ToxAV
{
  ToxAv *av;
  new_Tox *tox;
  std::map<int32_t, uint32_t> call_to_friend;
  std::map<uint32_t, int32_t> friend_to_call;

  struct
  {
    callback<toxav_call_cb> call;
    callback<toxav_call_state_cb> call_state;
    callback<toxav_request_audio_frame_cb> request_audio_frame;
    callback<toxav_request_video_frame_cb> request_video_frame;
    callback<toxav_receive_audio_frame_cb> receive_audio_frame;
    callback<toxav_receive_video_frame_cb> receive_video_frame;
  } callbacks;

  struct CB
  {
    static void callstate_OnInvite (void *agent, int32_t call_idx, void *userdata)
    {
      auto self = static_cast<new_ToxAV *> (userdata);

      auto found = self->call_to_friend.find (call_idx);
      assert (found == self->call_to_friend.end ());

      int peer_id = toxav_get_peer_id (self->av, call_idx, 0);
      self->call_to_friend[call_idx] = peer_id;
      self->friend_to_call[peer_id] = call_idx;

      auto cb = self->callbacks.call;
      cb.func (self, peer_id, cb.user_data);
    }

    static void callstate_OnRinging (void *agent, int32_t call_idx, void *userdata)
    {
      auto self = static_cast<new_ToxAV *> (userdata);

      auto found = self->call_to_friend.find (call_idx);
      assert (found != self->call_to_friend.end ());
    }

    static void callstate_OnStart (void *agent, int32_t call_idx, void *userdata)
    {
      auto self = static_cast<new_ToxAV *> (userdata);
      assert (false);
    }

    static void callstate_OnCancel (void *agent, int32_t call_idx, void *userdata)
    {
      auto self = static_cast<new_ToxAV *> (userdata);
      assert (false);
    }

    static void callstate_OnReject (void *agent, int32_t call_idx, void *userdata)
    {
      auto self = static_cast<new_ToxAV *> (userdata);
      assert (false);
    }

    static void callstate_OnEnd (void *agent, int32_t call_idx, void *userdata)
    {
      auto self = static_cast<new_ToxAV *> (userdata);
      assert (false);
    }

    static void callstate_OnRequestTimeout (void *agent, int32_t call_idx, void *userdata)
    {
      auto self = static_cast<new_ToxAV *> (userdata);
      assert (false);
    }

    static void callstate_OnPeerTimeout (void *agent, int32_t call_idx, void *userdata)
    {
      auto self = static_cast<new_ToxAV *> (userdata);
      assert (false);
    }

    static void callstate_OnPeerCSChange (void *agent, int32_t call_idx, void *userdata)
    {
      auto self = static_cast<new_ToxAV *> (userdata);
      assert (false);
    }

    static void callstate_OnSelfCSChange (void *agent, int32_t call_idx, void *userdata)
    {
      auto self = static_cast<new_ToxAV *> (userdata);
      assert (false);
    }

    static void audio (void *agent, int32_t call_idx, int16_t const *pcm, uint16_t size, void *userdata)
    {
      auto self = static_cast<new_ToxAV *> (userdata);
      assert (false);
    }

    static void video (void *agent, int32_t call_idx, vpx_image_t const *img, void *userdata)
    {
      auto self = static_cast<new_ToxAV *> (userdata);
      assert (false);
    }
  };

  new_ToxAV (ToxAv *av, new_Tox *tox)
    : av (av)
    , tox (tox)
  {
    toxav_register_callstate_callback (av, CB::callstate_OnInvite, av_OnInvite, this);
    toxav_register_callstate_callback (av, CB::callstate_OnRinging, av_OnRinging, this);
    toxav_register_callstate_callback (av, CB::callstate_OnStart, av_OnStart, this);
    toxav_register_callstate_callback (av, CB::callstate_OnCancel, av_OnCancel, this);
    toxav_register_callstate_callback (av, CB::callstate_OnReject, av_OnReject, this);
    toxav_register_callstate_callback (av, CB::callstate_OnEnd, av_OnEnd, this);
    toxav_register_callstate_callback (av, CB::callstate_OnRequestTimeout, av_OnRequestTimeout, this);
    toxav_register_callstate_callback (av, CB::callstate_OnPeerTimeout, av_OnPeerTimeout, this);
    toxav_register_callstate_callback (av, CB::callstate_OnPeerCSChange, av_OnPeerCSChange, this);
    toxav_register_callstate_callback (av, CB::callstate_OnSelfCSChange, av_OnSelfCSChange, this);
    toxav_register_audio_callback (av, CB::audio, this);
    toxav_register_video_callback (av, CB::video, this);
  }
};


new_ToxAV *
new_toxav_new (new_Tox *tox, TOXAV_ERR_NEW *error)
{
  if (!tox)
    {
      if (error) *error = TOXAV_ERR_NEW_NULL;
      return nullptr;
    }

  if (tox->has_av)
    {
      if (error) *error = TOXAV_ERR_NEW_MULTIPLE;
      return nullptr;
    }

  ToxAv *av = toxav_new (tox->tox, tox_count_friendlist (tox->tox) * 2 + 100);
  if (!av)
    {
      if (error) *error = TOXAV_ERR_NEW_MALLOC;
      return nullptr;
    }

  tox->has_av = true;

  if (error) *error = TOXAV_ERR_NEW_MALLOC;
  return new new_ToxAV (av, tox);
}

void
new_toxav_kill (new_ToxAV *av)
{
  toxav_kill (av->av);
  av->tox->has_av = false;
  delete av;
}


new_Tox *
new_toxav_get_tox (new_ToxAV *av)
{
  assert (false);
}

uint32_t
new_toxav_iteration_interval (new_ToxAV const *av)
{
  return toxav_do_interval (av->av);
}

void
new_toxav_iteration (new_ToxAV *av)
{
  toxav_do (av->av);
}

bool
new_toxav_call (new_ToxAV *av, uint32_t friend_number, uint32_t audio_bit_rate, uint32_t video_bit_rate, TOXAV_ERR_CALL *error)
{
  auto settings = ToxAvCSettings ();
  if (audio_bit_rate != 0)
    {
      settings.call_type = av_TypeAudio;
      settings.audio_bitrate = audio_bit_rate;
      settings.audio_frame_duration = 20;
      settings.audio_sample_rate = 48000;
      settings.audio_channels = 1;
    }
  if (video_bit_rate != 0)
    {
      settings.call_type = av_TypeVideo;
      settings.video_bitrate = video_bit_rate;
      settings.max_video_width = 640;
      settings.max_video_height = 480;
    }

  int call_index;
  if (toxav_call (av->av, &call_index, friend_number, &settings, 0x7fffffff) == -1)
    assert (false);

  av->call_to_friend[call_index] = friend_number;
  av->friend_to_call[friend_number] = call_index;

  if (error) *error = TOXAV_ERR_CALL_OK;
  return true;
}

void
new_toxav_callback_call (new_ToxAV *av, toxav_call_cb *function, void *user_data)
{
  av->callbacks.call = { function, user_data };
}

bool
new_toxav_answer (new_ToxAV *av, uint32_t friend_number, uint32_t audio_bit_rate, uint32_t video_bit_rate, TOXAV_ERR_ANSWER *error)
{
  auto found = av->friend_to_call.find (friend_number);
  if (found == av->friend_to_call.end ())
    {
      if (error) *error = TOXAV_ERR_ANSWER_FRIEND_NOT_CALLING;
      return false;
    }

  int32_t call_index = found->second;

  auto settings = ToxAvCSettings ();
  if (audio_bit_rate != 0)
    {
      settings.call_type = av_TypeAudio;
      settings.audio_bitrate = audio_bit_rate;
      settings.audio_frame_duration = 20;
      settings.audio_sample_rate = 48000;
      settings.audio_channels = 1;
    }
  if (video_bit_rate != 0)
    {
      settings.call_type = av_TypeVideo;
      settings.video_bitrate = video_bit_rate;
      settings.max_video_width = 640;
      settings.max_video_height = 480;
    }

  if (toxav_answer (av->av, call_index, &settings) == -1)
    assert (false);

  if (error) *error = TOXAV_ERR_ANSWER_OK;
  return true;
}

bool
new_toxav_call_control (new_ToxAV *av, uint32_t friend_number, TOXAV_CALL_CONTROL control, TOXAV_ERR_CALL_CONTROL *error)
{
  assert (false);
}

void
new_toxav_callback_call_state (new_ToxAV *av, toxav_call_state_cb *function, void *user_data)
{
  av->callbacks.call_state = { function, user_data };
}

bool
new_toxav_set_audio_bit_rate (new_ToxAV *av, uint32_t friend_number, uint32_t audio_bit_rate, TOXAV_ERR_BIT_RATE *error)
{
  assert (false);
}

bool
new_toxav_set_video_bit_rate (new_ToxAV *av, uint32_t friend_number, uint32_t video_bit_rate, TOXAV_ERR_BIT_RATE *error)
{
  assert (false);
}

void
new_toxav_callback_request_video_frame (new_ToxAV *av, toxav_request_video_frame_cb *function, void *user_data)
{
  av->callbacks.request_video_frame = { function, user_data };
}

bool
new_toxav_send_video_frame (new_ToxAV *av, uint32_t friend_number,
                            uint16_t width, uint16_t height,
                            uint8_t const *y, uint8_t const *u, uint8_t const *v, uint8_t const *a,
                            TOXAV_ERR_SEND_FRAME *error)
{
  assert (false);
}

void
new_toxav_callback_request_audio_frame (new_ToxAV *av, toxav_request_audio_frame_cb *function, void *user_data)
{
  av->callbacks.request_audio_frame = { function, user_data };
}

bool
new_toxav_send_audio_frame (new_ToxAV *av, uint32_t friend_number,
                            uint16_t const *pcm,
                            size_t sample_count,
                            uint8_t channels,
                            uint32_t sampling_rate,
                            TOXAV_ERR_SEND_FRAME *error)
{
  assert (false);
}

void
new_toxav_callback_receive_video_frame (new_ToxAV *av, toxav_receive_video_frame_cb *function, void *user_data)
{
  av->callbacks.receive_video_frame = { function, user_data };
}

void
new_toxav_callback_receive_audio_frame (new_ToxAV *av, toxav_receive_audio_frame_cb *function, void *user_data)
{
  av->callbacks.receive_audio_frame = { function, user_data };
}
