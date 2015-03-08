#include "ToxAv.h"


/*
 * Class:     im_tox_tox4j_ToxAvImpl
 * Method:    toxAvIterationInterval
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_ToxAvImpl_toxAvIterationInterval
  (JNIEnv *env, jclass, jint instanceNumber)
{
  return with_instance (env, instanceNumber, [=] (ToxAV *av, Events &events) {
    unused (events);
    return toxav_iteration_interval (av);
  });
}

/*
 * Class:     im_tox_tox4j_ToxAvImpl
 * Method:    toxAvIteration
 * Signature: (I)[B
 */
JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_ToxAvImpl_toxAvIteration
  (JNIEnv *env, jclass, jint instanceNumber)
{
  return with_instance (env, instanceNumber, [=] (ToxAV *av, Events &events) {
    toxav_iteration (av);

    std::vector<char> buffer (events.ByteSize ());
    events.SerializeToArray (buffer.data (), buffer.size ());
    events.Clear ();

    return toJavaArray (env, buffer);
  });
}

/*
 * Class:     im_tox_tox4j_ToxAvImpl
 * Method:    toxAvCall
 * Signature: (IIII)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxAvImpl_toxAvCall
  (JNIEnv *env, jclass, jint instanceNumber, jint friendNumber, jint audioBitRate, jint videoBitRate)
{
  return with_instance (env, instanceNumber, "Call", [] (TOXAV_ERR_CALL error) {
    switch (error) {
      success_case (CALL);
      failure_case (CALL, MALLOC);
      failure_case (CALL, FRIEND_NOT_FOUND);
      failure_case (CALL, FRIEND_NOT_CONNECTED);
      failure_case (CALL, FRIEND_ALREADY_IN_CALL);
      failure_case (CALL, INVALID_BIT_RATE);
    }
    return unhandled ();
  }, [] (bool) {
  }, toxav_call, friendNumber, audioBitRate, videoBitRate);
}

/*
 * Class:     im_tox_tox4j_ToxAvImpl
 * Method:    toxAvAnswer
 * Signature: (IIII)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxAvImpl_toxAvAnswer
  (JNIEnv *env, jclass, jint instanceNumber, jint friendNumber, jint audioBitRate, jint videoBitRate)
{
  return with_instance (env, instanceNumber, "Answer", [] (TOXAV_ERR_ANSWER error) {
    switch (error) {
      success_case (ANSWER);
      failure_case (ANSWER, MALLOC);
      failure_case (ANSWER, FRIEND_NOT_FOUND);
      failure_case (ANSWER, FRIEND_NOT_CALLING);
      failure_case (ANSWER, INVALID_BIT_RATE);
    }
    return unhandled ();
  }, [] (bool) {
  }, toxav_answer, friendNumber, audioBitRate, videoBitRate);
}

/*
 * Class:     im_tox_tox4j_ToxAvImpl
 * Method:    toxAvCallControl
 * Signature: (III)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxAvImpl_toxAvCallControl
  (JNIEnv *env, jclass, jint instanceNumber, jint friendNumber, jint control)
{
  return with_instance (env, instanceNumber, "CallControl", [] (TOXAV_ERR_CALL_CONTROL error) {
    switch (error) {
      success_case (CALL_CONTROL);
      failure_case (CALL_CONTROL, FRIEND_NOT_FOUND);
      failure_case (CALL_CONTROL, FRIEND_NOT_IN_CALL);
      failure_case (CALL_CONTROL, NOT_PAUSED);
      failure_case (CALL_CONTROL, DENIED);
      failure_case (CALL_CONTROL, ALREADY_PAUSED);
    }
    return unhandled ();
  }, [] (bool) {
  }, toxav_call_control, friendNumber, (TOXAV_CALL_CONTROL) control);
}

/*
 * Class:     im_tox_tox4j_ToxAvImpl
 * Method:    toxAvSetAudioBitRate
 * Signature: (III)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxAvImpl_toxAvSetAudioBitRate
  (JNIEnv *env, jclass, jint instanceNumber, jint friendNumber, jint audioBitRate)
{
  return with_instance (env, instanceNumber, "BitRate", [] (TOXAV_ERR_BIT_RATE error) {
    switch (error) {
      success_case (BIT_RATE);
      failure_case (BIT_RATE, INVALID);
    }
    return unhandled ();
  }, [] (bool) {
  }, toxav_set_audio_bit_rate, friendNumber, audioBitRate);
}

/*
 * Class:     im_tox_tox4j_ToxAvImpl
 * Method:    toxAvSetVideoBitRate
 * Signature: (III)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxAvImpl_toxAvSetVideoBitRate
  (JNIEnv *env, jclass, jint instanceNumber, jint friendNumber, jint videoBitRate)
{
  return with_instance (env, instanceNumber, "BitRate", [] (TOXAV_ERR_BIT_RATE error) {
    switch (error) {
      success_case (BIT_RATE);
      failure_case (BIT_RATE, INVALID);
    }
    return unhandled ();
  }, [] (bool) {
  }, toxav_set_video_bit_rate, friendNumber, videoBitRate);
}

/*
 * Class:     im_tox_tox4j_ToxAvImpl
 * Method:    toxAvSendVideoFrame
 * Signature: (IIII[B[B[B[B)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxAvImpl_toxAvSendVideoFrame
  (JNIEnv *env, jclass, jint instanceNumber, jint friendNumber, jint width, jint height, jbyteArray y, jbyteArray u, jbyteArray v, jbyteArray a)
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
      throw_tox_exception (env, tox_traits::module, "SendFrame", "BAD_LENGTH");
      return;
    }

  return with_instance (env, instanceNumber, "SendFrame", [] (TOXAV_ERR_SEND_FRAME error) {
    switch (error) {
      success_case (SEND_FRAME);
      failure_case (SEND_FRAME, NULL);
      failure_case (SEND_FRAME, FRIEND_NOT_FOUND);
      failure_case (SEND_FRAME, FRIEND_NOT_IN_CALL);
      failure_case (SEND_FRAME, NOT_REQUESTED);
      failure_case (SEND_FRAME, INVALID);
    }
    return unhandled ();
  }, [] (bool) {
  }, toxav_send_video_frame, friendNumber, width, height, yData.data (), uData.data (), vData.data (), aData.data ());
}

/*
 * Class:     im_tox_tox4j_ToxAvImpl
 * Method:    toxAvSendAudioFrame
 * Signature: (II[SIII)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxAvImpl_toxAvSendAudioFrame
  (JNIEnv *env, jclass, jint instanceNumber, jint friendNumber, jshortArray pcm, jint sampleCount, jint channels, jint samplingRate)
{
  assert (sampleCount >= 0);
  assert (channels >= 0);
  assert (channels <= 255);
  assert (samplingRate >= 0);

  ShortArray pcmData (env, pcm);
  if (pcmData.size () != size_t (sampleCount * channels))
    {
      throw_tox_exception (env, tox_traits::module, "SendFrame", "BAD_LENGTH");
      return;
    }

  return with_instance (env, instanceNumber, "SendFrame", [] (TOXAV_ERR_SEND_FRAME error) {
    switch (error) {
      success_case (SEND_FRAME);
      failure_case (SEND_FRAME, NULL);
      failure_case (SEND_FRAME, FRIEND_NOT_FOUND);
      failure_case (SEND_FRAME, FRIEND_NOT_IN_CALL);
      failure_case (SEND_FRAME, NOT_REQUESTED);
      failure_case (SEND_FRAME, INVALID);
    }
    return unhandled ();
  }, [] (bool) {
  }, toxav_send_audio_frame, friendNumber, pcmData.data (), sampleCount, channels, samplingRate);
}
