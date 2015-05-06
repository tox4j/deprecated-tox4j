#include "ToxCore.h"

using namespace core;


/*
 * Class:     im_tox_tox4j_impl_ToxCoreNative
 * Method:    toxFileControl
 * Signature: (IIII)V
 */
TOX_METHOD (void, FileControl,
  jint instanceNumber, jint friendNumber, jint fileNumber, jint control)
{
  TOX_FILE_CONTROL const file_control = [=] {
    switch (control)
      {
      case 0: return TOX_FILE_CONTROL_RESUME;
      case 1: return TOX_FILE_CONTROL_PAUSE;
      case 2: return TOX_FILE_CONTROL_CANCEL;
      }
    tox4j_fatal ("Invalid file control from Java");
  } ();

  return instances.with_instance_ign (env, instanceNumber, "FileControl",
    tox_file_control, friendNumber, fileNumber, file_control
  );
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreNative
 * Method:    toxFileSendSeek
 * Signature: (IIII)V
 */
TOX_METHOD (void, FileSendSeek,
  jint instanceNumber, jint friendNumber, jint fileNumber, jlong position)
{
  return instances.with_instance_ign (env, instanceNumber, "FileControl",
    tox_file_seek, friendNumber, fileNumber, position
  );
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreNative
 * Method:    toxFileSend
 * Signature: (IIIJ[B)I
 */
TOX_METHOD (jint, FileSend,
  jint instanceNumber, jint friendNumber, jint kind, jlong fileSize, jbyteArray fileId, jbyteArray filename)
{
  ByteArray fileIdData (env, fileId);
  ByteArray filenameData (env, filename);
  TOX_FILE_KIND const file_kind = [=] {
    switch (kind)
      {
      case 0: return TOX_FILE_KIND_DATA;
      case 1: return TOX_FILE_KIND_AVATAR;
      }
    tox4j_fatal ("Invalid file kind from Java");
  } ();

  return instances.with_instance_err (env, instanceNumber, "FileSend",
    identity,
    tox_file_send, friendNumber, file_kind, fileSize, fileIdData.data (), filenameData.data (), filenameData.size ()
  );
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreNative
 * Method:    toxFileSendChunk
 * Signature: (III[B)V
 */
TOX_METHOD (void, FileSendChunk,
  jint instanceNumber, jint friendNumber, jint fileNumber, jlong position, jbyteArray chunk)
{
  ByteArray chunkData (env, chunk);
  return instances.with_instance_ign (env, instanceNumber, "FileSendChunk",
    tox_file_send_chunk, friendNumber, fileNumber, position, chunkData.data (), chunkData.size ()
  );
}
