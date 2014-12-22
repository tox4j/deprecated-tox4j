#include <tox/av.h>
#include <tox/av_uncompat.h>

#include <tox/toxav.h>

#include "core_private.h"


struct new_ToxAV
{
  ToxAv *av;
  new_Tox *tox;

  new_ToxAV (ToxAv *av, new_Tox *tox)
    : av (av)
    , tox (tox)
  {
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
  return nullptr;
}

uint32_t
new_toxav_iteration_interval (new_ToxAV const *av)
{
  assert (false);
  return 0;
}

void
new_toxav_iteration (new_ToxAV *av)
{
  assert (false);
}

void
new_toxav_options_default (struct ToxAV_Options *options)
{
  assert (false);
}

struct ToxAV_Options *
new_toxav_options_new (TOXAV_ERR_OPTIONS_NEW *error)
{
  assert (false);
  return nullptr;
}

void
new_toxav_options_free (struct ToxAV_Options *options)
{
  assert (false);
}

TOXAV_ERR_OPTIONS
new_toxav_analyse_options (new_ToxAV *av, struct ToxAV_Options const *options)
{
  assert (false);
  return TOXAV_ERR_OPTIONS_OK;
}

bool
new_toxav_call (new_ToxAV *av, uint32_t friend_number, struct ToxAV_Options const *options, TOXAV_ERR_CALL *error)
{
  assert (false);
  return false;
}

void
new_toxav_callback_call (new_ToxAV *av, toxav_call_cb *function, void *user_data)
{
  assert (false);
}

bool
new_toxav_answer (new_ToxAV *av, uint32_t friend_number, struct ToxAV_Options const *options, TOXAV_ERR_ANSWER *error)
{
  assert (false);
  return false;
}

bool
new_toxav_call_control (new_ToxAV *av, uint32_t friend_number, TOXAV_CALL_CONTROL control, TOXAV_ERR_CALL_CONTROL *error)
{
  assert (false);
  return false;
}

void
new_toxav_callback_call_control (new_ToxAV *av, toxav_call_control_cb *function, void *user_data)
{
  assert (false);
}

bool
new_toxav_change_options (new_ToxAV *av, uint32_t friend_number, struct ToxAV_Options const *options, TOXAV_ERR_CHANGE_OPTIONS *error)
{
  assert (false);
  return false;
}

void
new_toxav_callback_change_options (new_ToxAV *av, toxav_change_options_cb *function, void *user_data)
{
  assert (false);
}

bool
new_toxav_friend_get_options (new_ToxAV *av, uint32_t friend_number, struct ToxAV_Options *options, TOXAV_ERR_FRIEND_GET_OPTIONS *error)
{
  assert (false);
  return false;
}

void
new_toxav_callback_request_video_frame (new_ToxAV *av, toxav_request_video_frame_cb *function, void *user_data)
{
  assert (false);
}

bool
new_toxav_send_video_frame (new_ToxAV *av, uint32_t friend_number, uint8_t const *y, uint8_t const *u, uint8_t const *v, uint8_t const *a, TOXAV_ERR_SEND_FRAME *error)
{
  assert (false);
  return false;
}

void
new_toxav_callback_request_audio_frame (new_ToxAV *av, toxav_request_audio_frame_cb *function, void *user_data)
{
  assert (false);
}

bool
new_toxav_send_audio_frame (new_ToxAV *av, uint32_t friend_number, uint16_t const *samples, TOXAV_ERR_SEND_FRAME *error)
{
  assert (false);
  return false;
}

void
new_toxav_callback_receive_video_frame (new_ToxAV *av, toxav_receive_video_frame_cb *function, void *user_data)
{
  assert (false);
}

void
new_toxav_callback_receive_audio_frame (new_ToxAV *av, toxav_receive_audio_frame_cb *function, void *user_data)
{
  assert (false);
}
