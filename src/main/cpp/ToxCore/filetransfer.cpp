#include "ToxCore.h"


/*
 * Class:     im_tox_tox4jToxCoreImpl
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
    fatal ("Invalid file control from Java");
  } ();

  return with_instance (env, instanceNumber, "FileControl",
    [] (TOX_ERR_FILE_CONTROL error)
      {
        switch (error)
          {
          success_case (FILE_CONTROL);
          failure_case (FILE_CONTROL, FRIEND_NOT_FOUND);
          failure_case (FILE_CONTROL, FRIEND_NOT_CONNECTED);
          failure_case (FILE_CONTROL, NOT_FOUND);
          failure_case (FILE_CONTROL, NOT_PAUSED);
          failure_case (FILE_CONTROL, DENIED);
          failure_case (FILE_CONTROL, ALREADY_PAUSED);
          failure_case (FILE_CONTROL, SENDQ);
          }
        return unhandled ();
      },
    tox_file_send_control, friendNumber, fileNumber, file_control
  );
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxFileSendSeek
 * Signature: (IIII)V
 */
TOX_METHOD (void, FileSendSeek,
  jint instanceNumber, jint friendNumber, jint fileNumber, jlong position)
{
  return with_instance (env, instanceNumber, "FileControl",
    [] (TOX_ERR_FILE_SEEK error)
      {
        switch (error)
          {
          success_case (FILE_SEEK);
          failure_case (FILE_SEEK, FRIEND_NOT_FOUND);
          failure_case (FILE_SEEK, FRIEND_NOT_CONNECTED);
          failure_case (FILE_SEEK, NOT_FOUND);
          failure_case (FILE_SEEK, DENIED);
          failure_case (FILE_SEEK, INVALID_POSITION);
          failure_case (FILE_SEEK, SENDQ);
          }
        return unhandled ();
      },
    tox_file_send_seek, friendNumber, fileNumber, position
  );
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
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
    fatal ("Invalid file kind from Java");
  } ();

  return with_instance (env, instanceNumber, "FileSend",
    [] (TOX_ERR_FILE_SEND error)
      {
        switch (error)
          {
          success_case (FILE_SEND);
          failure_case (FILE_SEND, NULL);
          failure_case (FILE_SEND, FRIEND_NOT_FOUND);
          failure_case (FILE_SEND, FRIEND_NOT_CONNECTED);
          failure_case (FILE_SEND, NAME_TOO_LONG);
          failure_case (FILE_SEND, TOO_MANY);
          }
        return unhandled ();
      },
    identity,
    tox_file_send, friendNumber, file_kind, fileSize, fileIdData.data (), filenameData.data (), filenameData.size ()
  );
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxFileSendChunk
 * Signature: (III[B)V
 */
TOX_METHOD (void, FileSendChunk,
  jint instanceNumber, jint friendNumber, jint fileNumber, jlong position, jbyteArray chunk)
{
  ByteArray chunkData (env, chunk);
  return with_instance (env, instanceNumber, "FileSendChunk",
    [] (TOX_ERR_FILE_SEND_CHUNK error)
      {
        switch (error)
          {
          success_case (FILE_SEND_CHUNK);
          failure_case (FILE_SEND_CHUNK, NULL);
          failure_case (FILE_SEND_CHUNK, FRIEND_NOT_FOUND);
          failure_case (FILE_SEND_CHUNK, FRIEND_NOT_CONNECTED);
          failure_case (FILE_SEND_CHUNK, NOT_FOUND);
          failure_case (FILE_SEND_CHUNK, NOT_TRANSFERRING);
          failure_case (FILE_SEND_CHUNK, INVALID_LENGTH);
          failure_case (FILE_SEND_CHUNK, SENDQ);
          failure_case (FILE_SEND_CHUNK, WRONG_POSITION);
          }
        return unhandled ();
      },
    tox_file_send_chunk, friendNumber, fileNumber, position, chunkData.data (), chunkData.size ()
  );
}
