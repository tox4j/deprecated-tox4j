#include "ToxAv.h"

using AvInstanceManager = instance_manager<av::tox_traits>;
using AvInstance = tox_instance<av::tox_traits>;


static void tox4j_call_cb(ToxAV *av, uint32_t friend_number, void *user_data)
{
    unused(av);
    Events &events = *static_cast<Events *>(user_data);
    auto msg = events.add_call();
    msg->set_friendnumber(friend_number);
}

static void tox4j_call_state_cb(ToxAV *av, uint32_t friend_number, TOXAV_CALL_STATE state, void *user_data)
{
    unused(av);
    Events &events = *static_cast<Events *>(user_data);
    auto msg = events.add_callstate();
    msg->set_friendnumber(friend_number);

    using im::tox::tox4j::proto::CallState;
    switch (state) {
#define call_state_case(STATE)                  \
        case TOXAV_CALL_STATE_##STATE:          \
            msg->set_state(CallState::STATE);   \
            break
        call_state_case(RINGING);
        call_state_case(NOT_SENDING);
        call_state_case(SENDING_A);
        call_state_case(SENDING_V);
        call_state_case(SENDING_AV);
        call_state_case(PAUSED);
        call_state_case(END);
        call_state_case(ERROR);
#undef call_state_case
    }
}

static void tox4j_request_audio_frame_cb(ToxAV *av, uint32_t friend_number, void *user_data)
{
    unused(av);
    Events &events = *static_cast<Events *>(user_data);
    auto msg = events.add_requestaudioframe();
    msg->set_friendnumber(friend_number);
}

static void tox4j_request_video_frame_cb(ToxAV *av, uint32_t friend_number, void *user_data)
{
    unused(av);
    Events &events = *static_cast<Events *>(user_data);
    auto msg = events.add_requestvideoframe();
    msg->set_friendnumber(friend_number);
}

static void tox4j_receive_audio_frame_cb(ToxAV *av, uint32_t friend_number,
                                         uint16_t const *pcm,
                                         size_t sample_count,
                                         uint8_t channels,
                                         uint32_t sampling_rate,
                                         void *user_data)
{
    unused(av);
    Events &events = *static_cast<Events *>(user_data);
    auto msg = events.add_receiveaudioframe();
    msg->set_friendnumber(friend_number);

    for (size_t i = 0; i < sample_count * channels; i++) {
        msg->add_pcm(pcm[i]);
    }

    msg->set_channels(channels);
    msg->set_samplingrate(sampling_rate);
}

static void tox4j_receive_video_frame_cb(ToxAV *av, uint32_t friend_number,
                                         uint16_t width, uint16_t height,
                                         uint8_t const *y, uint8_t const *u, uint8_t const *v, uint8_t const *a,
                                         void *user_data)
{
    unused(av);
    Events &events = *static_cast<Events *>(user_data);
    auto msg = events.add_receivevideoframe();
    msg->set_friendnumber(friend_number);
    msg->set_width(width);
    msg->set_height(height);
    msg->set_y(y, width * height);
    msg->set_u(u, width * height);
    msg->set_v(v, width * height);
    if (a != nullptr) {
        msg->set_a(a, width * height);
    }
}


/*
 * Class:     im_tox_tox4jToxAvImpl
 * Method:    destroyAll
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxAvImpl_destroyAll
  (JNIEnv *, jclass)
{
    std::lock_guard<std::mutex> lock(AvInstanceManager::self.mutex);
    AvInstanceManager::self.destroyAll();
}

/*
 * Class:     im_tox_tox4jToxAvImpl
 * Method:    toxAvNew
 * Signature: (ZZILjava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_ToxAvImpl_toxAvNew
  (JNIEnv *env, jclass, jint toxInstanceNumber)
{
    return core::with_instance(env, toxInstanceNumber, [=](Tox *tox, core::Events &) {
        TOXAV_ERR_NEW error;
        AvInstance::pointer av(toxav_new(tox, &error));

        std::unique_ptr<Events> events(new Events);

        // Set up our callbacks.
        toxav_callback_call               (av.get(), tox4j_call_cb,                events.get());
        toxav_callback_call_state         (av.get(), tox4j_call_state_cb,          events.get());
        toxav_callback_request_audio_frame(av.get(), tox4j_request_audio_frame_cb, events.get());
        toxav_callback_request_video_frame(av.get(), tox4j_request_video_frame_cb, events.get());
        toxav_callback_receive_audio_frame(av.get(), tox4j_receive_audio_frame_cb, events.get());
        toxav_callback_receive_video_frame(av.get(), tox4j_receive_video_frame_cb, events.get());

        AvInstance instance {
            std::move(av),
            std::move(events),
            std::unique_ptr<std::mutex>(new std::mutex)
        };

        return AvInstanceManager::self.add(std::move(instance));
    });
}

/*
 * Class:     im_tox_tox4jToxAvImpl
 * Method:    toxAvKill
 * Signature: (I)I
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxAvImpl_toxAvKill
  (JNIEnv *env, jclass, jint instanceNumber)
{
    AvInstanceManager::self.kill(env, instanceNumber);
}

/*
 * Class:     im_tox_tox4jToxAvImpl
 * Method:    finalize
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxAvImpl_finalize
  (JNIEnv *env, jclass, jint instanceNumber)
{
    AvInstanceManager::self.finalize(env, instanceNumber);
}
