#include "ToxCore.h"


/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxFileControl
 * Signature: (IIII)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxFileControl
  (JNIEnv *env, jclass, jint instanceNumber, jint friendNumber, jint fileNumber, jint control)
{
  return with_instance (env, instanceNumber, "FileControl", [] (TOX_ERR_FILE_CONTROL error) {
    switch (error) {
      success_case (FILE_CONTROL);
      failure_case (FILE_CONTROL, FRIEND_NOT_FOUND);
      failure_case (FILE_CONTROL, FRIEND_NOT_CONNECTED);
      failure_case (FILE_CONTROL, NOT_FOUND);
      failure_case (FILE_CONTROL, NOT_PAUSED);
      failure_case (FILE_CONTROL, DENIED);
      failure_case (FILE_CONTROL, ALREADY_PAUSED);
      failure_case (FILE_CONTROL, SEND_FAILED);
    }
    return unhandled ();
  }, [] (bool) {
  }, tox_file_control, friendNumber, fileNumber, (TOX_FILE_CONTROL)control); // TODO: check valid values for control?
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxFileSend
 * Signature: (IIIJ[B)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxFileSend
  (JNIEnv *env, jclass, jint instanceNumber, jint friendNumber, jint kind, jlong fileSize, jbyteArray filename)
{
  ByteArray filenameData (env, filename);
  return with_instance (env, instanceNumber, "FileSend", [] (TOX_ERR_FILE_SEND error) {
    switch (error) {
      success_case (FILE_SEND);
      failure_case (FILE_SEND, NULL);
      failure_case (FILE_SEND, FRIEND_NOT_FOUND);
      failure_case (FILE_SEND, FRIEND_NOT_CONNECTED);
      failure_case (FILE_SEND, NAME_EMPTY);
      failure_case (FILE_SEND, NAME_TOO_LONG);
      failure_case (FILE_SEND, TOO_MANY);
    }
    return unhandled ();
  }, [] (uint32_t file_number) {
    return file_number;
  }, tox_file_send, friendNumber, (TOX_FILE_KIND)kind, fileSize, filenameData.data (), filenameData.size ()); // TODO: check valid values for kind?
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxFileSendChunk
 * Signature: (III[B)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxFileSendChunk
  (JNIEnv *env, jclass, jint instanceNumber, jint friendNumber, jint fileNumber, jbyteArray chunk)
{
  ByteArray chunkData (env, chunk);
  return with_instance (env, instanceNumber, "FileSendChunk", [] (TOX_ERR_FILE_SEND_CHUNK error) {
    switch (error) {
      success_case (FILE_SEND_CHUNK);
      failure_case (FILE_SEND_CHUNK, NULL);
      failure_case (FILE_SEND_CHUNK, FRIEND_NOT_FOUND);
      failure_case (FILE_SEND_CHUNK, FRIEND_NOT_CONNECTED);
      failure_case (FILE_SEND_CHUNK, NOT_FOUND);
      failure_case (FILE_SEND_CHUNK, NOT_TRANSFERRING);
      failure_case (FILE_SEND_CHUNK, TOO_LARGE);
      failure_case (FILE_SEND_CHUNK, SENDQ);
    }
    return unhandled ();
  }, [] (bool) {
  }, tox_file_send_chunk, friendNumber, fileNumber, chunkData.data (), chunkData.size ());
}
