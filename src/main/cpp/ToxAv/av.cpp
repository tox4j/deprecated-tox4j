#include "ToxAv.h"

static std::string
fullMessage(jint instance_number, char const *message)
{
    std::ostringstream result;
    result << message << ", instance_number = " << instance_number;
    return result.str();
}

static void
throw_exception(JNIEnv *env, jint instance_number, char const *class_name, char const *message)
{
    env->ThrowNew(env->FindClass(class_name), fullMessage(instance_number, message).c_str());
}


void
throw_unsupported_operation_exception(JNIEnv *env, jint instance_number, char const *message)
{
    throw_exception(env, instance_number, "java/lang/UnsupportedOperationException", message);
}


/*
 * Class:     im_tox_tox4j_ToxAvImpl
 * Method:    toxAvIterationInterval
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_ToxAvImpl_toxAvIterationInterval
  (JNIEnv *env, jclass, jint instanceNumber)
{
    return with_instance(env, instanceNumber, [=](ToxAV *av, Events &events) {
        unused(events);
        return toxav_iteration_interval(av);
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
    return with_instance(env, instanceNumber, [=](ToxAV *av, Events &events) {
        toxav_iteration(av);

        std::vector<char> buffer(events.ByteSize());
        events.SerializeToArray(buffer.data(), buffer.size());
        events.Clear();

        return toJavaArray(env, buffer);
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
    return with_instance(env, instanceNumber, "Call", [](TOXAV_ERR_CALL error) {
        switch (error) {
            success_case(CALL);
            failure_case(CALL, MALLOC);
            failure_case(CALL, FRIEND_NOT_FOUND);
            failure_case(CALL, FRIEND_NOT_CONNECTED);
            failure_case(CALL, FRIEND_ALREADY_IN_CALL);
            failure_case(CALL, INVALID_BIT_RATE);
        }
        return unhandled();
    }, [](bool) {
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
    return with_instance(env, instanceNumber, "Answer", [](TOXAV_ERR_ANSWER error) {
        switch (error) {
            success_case(ANSWER);
            failure_case(ANSWER, MALLOC);
            failure_case(ANSWER, FRIEND_NOT_FOUND);
            failure_case(ANSWER, FRIEND_NOT_CALLING);
            failure_case(ANSWER, INVALID_BIT_RATE);
        }
        return unhandled();
    }, [](bool) {
    }, toxav_answer, friendNumber, audioBitRate, videoBitRate);
}

/*
 * Class:     im_tox_tox4j_ToxAvImpl
 * Method:    toxAvCallControl
 * Signature: (III)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxAvImpl_toxAvCallControl
  (JNIEnv *env, jclass, jint instanceNumber, jint friendNumber, jint)
{
    return with_instance(env, instanceNumber, [=](ToxAV *av, Events &events) {
        unused(av);
        unused(events);
        throw_unsupported_operation_exception(env, instanceNumber, "toxAvCallControl");
    });
}

/*
 * Class:     im_tox_tox4j_ToxAvImpl
 * Method:    toxAvSetAudioBitRate
 * Signature: (III)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxAvImpl_toxAvSetAudioBitRate
  (JNIEnv *env, jclass, jint instanceNumber, jint friendNumber, jint)
{
    return with_instance(env, instanceNumber, [=](ToxAV *av, Events &events) {
        unused(av);
        unused(events);
        throw_unsupported_operation_exception(env, instanceNumber, "toxAvSetAudioBitRate");
    });
}

/*
 * Class:     im_tox_tox4j_ToxAvImpl
 * Method:    toxAvSetVideoBitRate
 * Signature: (III)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxAvImpl_toxAvSetVideoBitRate
  (JNIEnv *env, jclass, jint instanceNumber, jint friendNumber, jint)
{
    return with_instance(env, instanceNumber, [=](ToxAV *av, Events &events) {
        unused(av);
        unused(events);
        throw_unsupported_operation_exception(env, instanceNumber, "toxAvSetVideoBitRate");
    });
}

/*
 * Class:     im_tox_tox4j_ToxAvImpl
 * Method:    toxAvSendVideoFrame
 * Signature: (IIII[B[B[B[B)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxAvImpl_toxAvSendVideoFrame
  (JNIEnv *env, jclass, jint instanceNumber, jint friendNumber, jint, jint, jbyteArray, jbyteArray, jbyteArray, jbyteArray)
{
    return with_instance(env, instanceNumber, [=](ToxAV *av, Events &events) {
        unused(av);
        unused(events);
        throw_unsupported_operation_exception(env, instanceNumber, "toxAvSendVideoFrame");
    });
}

/*
 * Class:     im_tox_tox4j_ToxAvImpl
 * Method:    toxAvSendAudioFrame
 * Signature: (II[BIII)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxAvImpl_toxAvSendAudioFrame
  (JNIEnv *env, jclass, jint instanceNumber, jint friendNumber, jbyteArray, jint, jint, jint)
{
    return with_instance(env, instanceNumber, [=](ToxAV *av, Events &events) {
        unused(av);
        unused(events);
        throw_unsupported_operation_exception(env, instanceNumber, "toxAvSendAudioFrame");
    });
}
