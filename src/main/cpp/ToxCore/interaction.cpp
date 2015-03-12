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
handle_send_message_error (TOX_ERR_SEND_MESSAGE error)
{
  switch (error)
    {
    success_case (SEND_MESSAGE);
    failure_case (SEND_MESSAGE, NULL);
    failure_case (SEND_MESSAGE, FRIEND_NOT_FOUND);
    failure_case (SEND_MESSAGE, FRIEND_NOT_CONNECTED);
    failure_case (SEND_MESSAGE, SENDQ);
    failure_case (SEND_MESSAGE, TOO_LONG);
    failure_case (SEND_MESSAGE, EMPTY);
    }

  return unhandled ();
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxSendMessage
 * Signature: (II[B)I
 */
TOX_METHOD (jint, SendMessage,
  jint instanceNumber, jint friendNumber, jbyteArray message)
{
  ByteArray message_array (env, message);
  return with_instance (env, instanceNumber, "SendMessage",
    handle_send_message_error,
    identity,
    tox_send_message, friendNumber, message_array.data (), message_array.size ()
  );
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxSendAction
 * Signature: (II[B)I
 */
TOX_METHOD (jint, SendAction,
  jint instanceNumber, jint friendNumber, jbyteArray action)
{
  ByteArray action_array (env, action);
  return with_instance (env, instanceNumber, "SendMessage",
    handle_send_message_error,
    identity,
    tox_send_action, friendNumber, action_array.data (), action_array.size ()
  );
}
