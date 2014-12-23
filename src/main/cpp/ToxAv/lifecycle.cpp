#include "ToxAv.h"

using AvInstanceManager = instance_manager<av::tox_traits>;
using AvInstance = tox_instance<av::tox_traits>;

/*
 * Class:     im_tox_tox4jToxAvImpl
 * Method:    destroyAll
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxAvImpl_destroyAll
  (JNIEnv *, jclass)
{
    std::unique_lock<std::mutex> lock(AvInstanceManager::self.mutex);
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
