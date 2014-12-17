#include "tox4j/Tox4j.h"
#include "jniutil.h"


/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxFileControl
 * Signature: (IIBI)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxFileControl
  (JNIEnv *env, jclass, jint instanceNumber, jint, jbyte, jint)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_callback_lossless_packet);
        throw_unsupported_operation_exception(env, instanceNumber, "toxFileControl");
    });
}

/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxFileSend
 * Signature: (IIIJ[B)B
 */
JNIEXPORT jbyte JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxFileSend
  (JNIEnv *env, jclass, jint instanceNumber, jint friendNumber, jint kind, jlong fileSize, jbyteArray filename)
{
    ByteArray filenameData(env, filename);
    return with_instance(env, instanceNumber, "FileSend", [](TOX_ERR_FILE_SEND error) {
        switch (error) {
            case TOX_ERR_FILE_SEND_OK:
                return success();
            case TOX_ERR_FILE_SEND_NULL:
                return failure("NULL");
            case TOX_ERR_FILE_SEND_FRIEND_NOT_FOUND:
                return failure("FRIEND_NOT_FOUND");
            case TOX_ERR_FILE_SEND_FRIEND_NOT_CONNECTED:
                return failure("FRIEND_NOT_CONNECTED");
            case TOX_ERR_FILE_SEND_NAME_EMPTY:
                return failure("EMPTY");
            case TOX_ERR_FILE_SEND_NAME_TOO_LONG:
                return failure("TOO_LONG");
            case TOX_ERR_FILE_SEND_TOO_MANY:
                return failure("TOO_MANY");
        }
        return unhandled();
    }, [](uint8_t file_number) {
        return file_number;
    }, tox_file_send, friendNumber, (TOX_FILE_KIND) kind, fileSize, filenameData.data(), filenameData.size());
}

/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxFileSendChunk
 * Signature: (IIB[B)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxFileSendChunk
  (JNIEnv *env, jclass, jint instanceNumber, jint, jbyte, jbyteArray)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_callback_lossless_packet);
        throw_unsupported_operation_exception(env, instanceNumber, "toxFileSendChunk");
    });
}
