#include "tox4j/Tox4j.h"
#include "jniutil.h"

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxSave
 * Signature: (I)[B
 */
JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxSave
  (JNIEnv *env, jclass, jint instanceNumber)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(events);
        std::vector<uint8_t> buffer(tox_save_size(tox));
        tox_save(tox, buffer.data());

        return toJavaArray(env, buffer);
    });
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxLoad
 * Signature: (I[B)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxLoad
  (JNIEnv *env, jclass, jint instanceNumber, jbyteArray data)
{
    ByteArray bytes(env, data);
    return with_instance(env, instanceNumber, "Load", [](TOX_ERR_LOAD error) {
        switch (error) {
            case TOX_ERR_LOAD_OK:
                return success();
            case TOX_ERR_LOAD_NULL:
                return failure("NULL");
            case TOX_ERR_LOAD_ENCRYPTED:
                return failure("ENCRYPTED");
            case TOX_ERR_LOAD_BAD_FORMAT:
                return failure("BAD_FORMAT");
        }
        return unhandled();
    }, [](bool) {
    }, tox_load, bytes.data(), bytes.size());
}
