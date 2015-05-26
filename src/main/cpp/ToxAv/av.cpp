#include "ToxAv.h"

#ifdef TOXAV_VERSION_MAJOR

using namespace av;

/*
 * Class:     im_tox_tox4j_impl_ToxAvJni
 * Method:    toxavIterationInterval
 * Signature: (I)I
 */
TOX_METHOD (jint, IterationInterval,
  jint instanceNumber)
{
  return instances.with_instance_noerr (env, instanceNumber,
    toxav_iteration_interval);
}

/*
 * Class:     im_tox_tox4j_impl_ToxAvJni
 * Method:    toxavIterate
 * Signature: (I)[B
 */
TOX_METHOD (jbyteArray, Iterate,
  jint instanceNumber)
{
  return instances.with_instance (env, instanceNumber,
    [=] (ToxAV *av, Events &events) -> jbyteArray
      {
        toxav_iterate (av);
        if (events.ByteSize () == 0)
          return nullptr;

        std::vector<char> buffer (events.ByteSize ());
        events.SerializeToArray (buffer.data (), buffer.size ());
        events.Clear ();

        return toJavaArray (env, buffer);
      }
  );
}

/*
 * Class:     im_tox_tox4j_impl_ToxAvJni
 * Method:    toxavCall
 * Signature: (IIII)V
 */
TOX_METHOD (void, Call,
  jint instanceNumber, jint friendNumber, jint audioBitRate, jint videoBitRate)
{
  return instances.with_instance_ign (env, instanceNumber,
    toxav_call, friendNumber, audioBitRate, videoBitRate
  );
}

/*
 * Class:     im_tox_tox4j_impl_ToxAvJni
 * Method:    toxavAnswer
 * Signature: (IIII)V
 */
TOX_METHOD (void, Answer,
  jint instanceNumber, jint friendNumber, jint audioBitRate, jint videoBitRate)
{
  return instances.with_instance_ign (env, instanceNumber,
    toxav_answer, friendNumber, audioBitRate, videoBitRate
  );
}

/*
 * Class:     im_tox_tox4j_impl_ToxAvJni
 * Method:    toxavCallControl
 * Signature: (III)V
 */
TOX_METHOD (void, CallControl,
  jint instanceNumber, jint friendNumber, jint control)
{
  TOXAV_CALL_CONTROL call_control = [=] {
    switch (control)
      {
      case 0: return TOXAV_CALL_CONTROL_RESUME;
      case 1: return TOXAV_CALL_CONTROL_PAUSE;
      case 2: return TOXAV_CALL_CONTROL_CANCEL;
      case 3: return TOXAV_CALL_CONTROL_MUTE_AUDIO;
      case 4: return TOXAV_CALL_CONTROL_UNMUTE_AUDIO;
      case 5: return TOXAV_CALL_CONTROL_HIDE_VIDEO;
      case 6: return TOXAV_CALL_CONTROL_SHOW_VIDEO;
      }
    tox4j_fatal ("Invalid call control from Java");
  } ();
  return instances.with_instance_ign (env, instanceNumber,
    toxav_call_control, friendNumber, call_control
  );
}

/*
 * Class:     im_tox_tox4j_impl_ToxAvJni
 * Method:    toxavAudioBitRateSet
 * Signature: (IIIZ)V
 */
TOX_METHOD (void, AudioBitRateSet,
  jint instanceNumber, jint friendNumber, jint audioBitRate, jboolean force)
{
  return instances.with_instance_ign (env, instanceNumber,
    toxav_audio_bit_rate_set, friendNumber, audioBitRate, force
  );
}

/*
 * Class:     im_tox_tox4j_impl_ToxAvJni
 * Method:    toxavVideoBitRateSet
 * Signature: (IIIZ)V
 */
TOX_METHOD (void, VideoBitRateSet,
  jint instanceNumber, jint friendNumber, jint videoBitRate, jboolean force)
{
  return instances.with_instance_ign (env, instanceNumber,
    toxav_video_bit_rate_set, friendNumber, videoBitRate, force
  );
}

/*
 * Class:     im_tox_tox4j_impl_ToxAvJni
 * Method:    toxavAudioSendFrame
 * Signature: (II[SIII)V
 */
TOX_METHOD (void, AudioSendFrame,
  jint instanceNumber, jint friendNumber, jshortArray pcm, jint sampleCount, jint channels, jint samplingRate)
{
  tox4j_assert (sampleCount >= 0);
  tox4j_assert (channels >= 0);
  tox4j_assert (channels <= 255);
  tox4j_assert (samplingRate >= 0);

  ShortArray pcmData (env, pcm);
  if (pcmData.size () != size_t (sampleCount * channels))
    {
      throw_tox_exception (env, module_name<ToxAV>, method_name<TOXAV_ERR_SEND_FRAME>, "BAD_LENGTH");
      return;
    }

  return instances.with_instance_ign (env, instanceNumber,
    toxav_audio_send_frame, friendNumber, pcmData.data (), sampleCount, channels, samplingRate
  );
}

/*
 * Class:     im_tox_tox4j_impl_ToxAvJni
 * Method:    toxavVideoSendFrame
 * Signature: (IIII[B[B[B[B)V
 */
TOX_METHOD (void, VideoSendFrame,
  jint instanceNumber, jint friendNumber, jint width, jint height, jbyteArray y, jbyteArray u, jbyteArray v, jbyteArray a)
{
  size_t pixel_count = width * height;

  ByteArray yData (env, y);
  ByteArray uData (env, u);
  ByteArray vData (env, v);
  ByteArray aData (env, a);
  if (yData.size () != pixel_count ||
      uData.size () != pixel_count ||
      vData.size () != pixel_count ||
      (!aData.empty () && aData.size () != pixel_count))
    {
      throw_tox_exception (env, module_name<ToxAV>, method_name<TOXAV_ERR_SEND_FRAME>, "BAD_LENGTH");
      return;
    }

  return instances.with_instance_ign (env, instanceNumber,
    toxav_video_send_frame, friendNumber, width, height, yData.data (), uData.data (), vData.data (), aData.data ()
  );
}
#endif
