#include "ToxCore.h"

using namespace core;


/*
 * Class:     im_tox_tox4j_impl_ToxCoreJni
 * Method:    toxSelfSetTyping
 * Signature: (IIZ)V
 */
TOX_METHOD (void, SelfSetTyping,
  jint instanceNumber, jint friendNumber, jboolean isTyping)
{
  return instances.with_instance_ign (env, instanceNumber, "SetTyping",
    tox_self_set_typing, friendNumber, isTyping
  );
}


/*
 * Class:     im_tox_tox4j_impl_ToxCoreJni
 * Method:    toxSendMessage
 * Signature: (IIII[B)I
 */
TOX_METHOD (jint, SendMessage,
  jint instanceNumber, jint friendNumber, jint type, jint timeDelta, jbyteArray message)
{
  ByteArray const message_array (env, message);
  TOX_MESSAGE_TYPE const message_type = [=] {
    switch (type)
      {
      case 0: return TOX_MESSAGE_TYPE_NORMAL;
      case 1: return TOX_MESSAGE_TYPE_ACTION;
      }
    tox4j_fatal ("Invalid message type from Java");
  } ();

  return instances.with_instance_err (env, instanceNumber, "SendMessage",
    identity,
    tox_friend_send_message, friendNumber, message_type, message_array.data (), message_array.size ()
  );
}
