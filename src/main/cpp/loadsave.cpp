#include "Tox4j.h"

/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxSave
 * Signature: (I)[B
 */
JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxSave
  (JNIEnv *env, jclass, jint instanceNumber)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(events);
        std::vector<uint8_t> buffer(tox_save_size(tox));
        tox_save(tox, buffer.data());

        return toByteArray(env, buffer);
    });
}

/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxLoad
 * Signature: (I[B)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxLoad
  (JNIEnv *env, jclass, jint instanceNumber, jbyteArray data)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(events);
        ByteArray bytes(env, data);
        TOX_ERR_LOAD error;
        tox_load(tox, bytes.data(), bytes.size(), &error);
        switch (error) {
            case TOX_ERR_LOAD_OK:
                return;
            case TOX_ERR_LOAD_NULL:
                throw_tox_exception(env, "Load", "NULL");
                return;
            case TOX_ERR_LOAD_ENCRYPTED:
                throw_tox_exception(env, "Load", "ENCRYPTED");
                return;
            case TOX_ERR_LOAD_BAD_FORMAT:
                throw_tox_exception(env, "Load", "BAD_FORMAT");
                return;
        }

        throw_illegal_state_exception(env, error, "Unknown error code");
    });
}
