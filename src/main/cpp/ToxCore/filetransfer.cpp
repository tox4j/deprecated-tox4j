#include "ToxCore.h"

using namespace core;


/*
 * Class:     im_tox_tox4j_impl_ToxCoreJni
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

  return instances.with_instance_ign (env, instanceNumber,
    tox_file_control, friendNumber, fileNumber, file_control
  );
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreJni
 * Method:    toxFileSeek
 * Signature: (IIII)V
 */
TOX_METHOD (void, FileSeek,
  jint instanceNumber, jint friendNumber, jint fileNumber, jlong position)
{
  return instances.with_instance_ign (env, instanceNumber,
    tox_file_seek, friendNumber, fileNumber, position
  );
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreJni
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

  // In Java, we only have 63 bit positive file sizes, so all negative values
  // are streaming.
  if (fileSize < 0)
    fileSize = -1;

  return instances.with_instance_err (env, instanceNumber,
    identity,
    tox_file_send, friendNumber, file_kind, fileSize, fileIdData.data (), filenameData.data (), filenameData.size ()
  );
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreJni
 * Method:    toxFileSendChunk
 * Signature: (III[B)V
 */
TOX_METHOD (void, FileSendChunk,
  jint instanceNumber, jint friendNumber, jint fileNumber, jlong position, jbyteArray chunk)
{
  ByteArray chunkData (env, chunk);
  return instances.with_instance_ign (env, instanceNumber,
    tox_file_send_chunk, friendNumber, fileNumber, position, chunkData.data (), chunkData.size ()
  );
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreJni
 * Method:    toxFileGetFileId
 * Signature: (II)[B
 */
TOX_METHOD (jbyteArray, FileGetFileId,
  jint instanceNumber, jint friendNumber, jint fileNumber)
{
  std::vector<uint8_t> file_id (TOX_FILE_ID_LENGTH);
  return instances.with_instance_err (env, instanceNumber,
    [env, &file_id] (bool)
      {
        return toJavaArray (env, file_id);
      },
    tox_file_get_file_id, friendNumber, fileNumber, file_id.data ()
  );
}
