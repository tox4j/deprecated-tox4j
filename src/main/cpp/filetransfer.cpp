#include "tox4j/Tox4j.h"
#include "jniutil.h"


/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxFileControl
 * Signature: (IIII)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxFileControl
  (JNIEnv *env, jclass, jint instanceNumber, jint friendNumber, jint fileNumber, jint control)
{
    return with_instance(env, instanceNumber, "FileControl", [](TOX_ERR_FILE_CONTROL error) {
        switch (error) {
            case TOX_ERR_FILE_CONTROL_OK:
                return success();
            case TOX_ERR_FILE_CONTROL_FRIEND_NOT_FOUND:
                return failure("FRIEND_NOT_FOUND");
            case TOX_ERR_FILE_CONTROL_FRIEND_NOT_CONNECTED:
                return failure("FRIEND_NOT_CONNECTED");
            case TOX_ERR_FILE_CONTROL_NOT_FOUND:
                return failure("NOT_FOUND");
            case TOX_ERR_FILE_CONTROL_NOT_PAUSED:
                return failure("NOT_PAUSED");
            case TOX_ERR_FILE_CONTROL_DENIED:
                return failure("DENIED");
            case TOX_ERR_FILE_CONTROL_ALREADY_PAUSED:
                return failure("ALREADY_PAUSED");
        }
        return unhandled();
    }, [](bool) {
    }, tox_file_control, friendNumber, fileNumber, (TOX_FILE_CONTROL) control);
}

/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxFileSend
 * Signature: (IIIJ[B)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxFileSend
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
    }, [](uint32_t file_number) {
        return file_number;
    }, tox_file_send, friendNumber, (TOX_FILE_KIND) kind, fileSize, filenameData.data(), filenameData.size());
}

/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxFileSendChunk
 * Signature: (III[B)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxFileSendChunk
  (JNIEnv *env, jclass, jint instanceNumber, jint friendNumber, jint fileNumber, jbyteArray chunk)
{
    ByteArray chunkData(env, chunk);
    return with_instance(env, instanceNumber, "FileSendChunk", [](TOX_ERR_FILE_SEND_CHUNK error) {
        switch (error) {
            case TOX_ERR_FILE_SEND_CHUNK_OK:
                return success();
            case TOX_ERR_FILE_SEND_CHUNK_NULL:
                return failure("NULL");
            case TOX_ERR_FILE_SEND_CHUNK_FRIEND_NOT_FOUND:
                return failure("FRIEND_NOT_FOUND");
            case TOX_ERR_FILE_SEND_CHUNK_FRIEND_NOT_CONNECTED:
                return failure("FRIEND_NOT_CONNECTED");
            case TOX_ERR_FILE_SEND_CHUNK_NOT_FOUND:
                return failure("NOT_FOUND");
        }
        return unhandled();
    }, [](bool) {
    }, tox_file_send_chunk, friendNumber, fileNumber, chunkData.data(), chunkData.size());
}
