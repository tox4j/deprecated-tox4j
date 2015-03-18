#include "ToxCore.h"


/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxSelfSetTyping
 * Signature: (IIZ)V
 */
TOX_METHOD (void, SelfSetTyping,
  jint instanceNumber, jint friendNumber, jboolean isTyping)
{
  return with_instance (env, instanceNumber, "SetTyping",
    [] (TOX_ERR_SET_TYPING error)
      {
        switch (error) {
          success_case (SET_TYPING);
          failure_case (SET_TYPING, FRIEND_NOT_FOUND);
        }
        return unhandled ();
      },
    tox_self_set_typing, friendNumber, isTyping
  );
}


static ErrorHandling
handle_send_message_error (TOX_ERR_FRIEND_SEND_MESSAGE error)
{
  switch (error)
    {
    success_case (FRIEND_SEND_MESSAGE);
    failure_case (FRIEND_SEND_MESSAGE, NULL);
    failure_case (FRIEND_SEND_MESSAGE, FRIEND_NOT_FOUND);
    failure_case (FRIEND_SEND_MESSAGE, FRIEND_NOT_CONNECTED);
    failure_case (FRIEND_SEND_MESSAGE, SENDQ);
    failure_case (FRIEND_SEND_MESSAGE, TOO_LONG);
    failure_case (FRIEND_SEND_MESSAGE, EMPTY);
    }

  return unhandled ();
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxSendMessage
 * Signature: (III[B)I
 */
TOX_METHOD (jint, SendMessage,
  jint instanceNumber, jint friendNumber, jint type, jbyteArray message)
{
  ByteArray const message_array (env, message);
  TOX_MESSAGE_TYPE const message_type = [=] {
    switch (type)
      {
      case 0: return TOX_MESSAGE_TYPE_NORMAL;
      case 1: return TOX_MESSAGE_TYPE_ACTION;
      }
    fatal ("Invalid message type from Java");
  } ();

  return with_instance (env, instanceNumber, "SendMessage",
    handle_send_message_error,
    identity,
    tox_friend_send_message, friendNumber, message_type, message_array.data (), message_array.size ()
  );
}
