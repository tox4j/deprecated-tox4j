#include "tox4j/Tox4j.h"
#include "jniutil.h"


/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxSetTyping
 * Signature: (IIZ)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxSetTyping
  (JNIEnv *env, jclass, jint instanceNumber, jint friendNumber, jboolean isTyping)
{
    return with_instance(env, instanceNumber, "SetTyping", [](TOX_ERR_SET_TYPING error) {
        switch (error) {
            case TOX_ERR_SET_TYPING_OK:
                return success();
            case TOX_ERR_SET_TYPING_FRIEND_NOT_FOUND:
                return failure("FRIEND_NOT_FOUND");
        }
        return unhandled();
    }, [](bool) {
    }, tox_set_typing, friendNumber, isTyping);
}


static ErrorHandling
handle_send_message_error(TOX_ERR_SEND_MESSAGE error)
{
    switch (error) {
        case TOX_ERR_SEND_MESSAGE_OK:
            return success();
        case TOX_ERR_SEND_MESSAGE_NULL:
            return failure("NULL");
        case TOX_ERR_SEND_MESSAGE_FRIEND_NOT_FOUND:
            return failure("FRIEND_NOT_FOUND");
        case TOX_ERR_SEND_MESSAGE_FRIEND_NOT_CONNECTED:
            return failure("FRIEND_NOT_CONNECTED");
        case TOX_ERR_SEND_MESSAGE_SENDQ:
            return failure("SENDQ");
        case TOX_ERR_SEND_MESSAGE_TOO_LONG:
            return failure("TOO_LONG");
        case TOX_ERR_SEND_MESSAGE_EMPTY:
            return failure("EMPTY");
    }
    return unhandled();
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxSendMessage
 * Signature: (II[B)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxSendMessage
  (JNIEnv *env, jclass, jint instanceNumber, jint friendNumber, jbyteArray message)
{
    ByteArray message_array(env, message);
    return with_instance(env, instanceNumber, "SendMessage", handle_send_message_error, [](uint32_t message_id) {
        return message_id;
    }, tox_send_message, friendNumber, message_array.data(), message_array.size());
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxSendAction
 * Signature: (II[B)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxSendAction
  (JNIEnv *env, jclass, jint instanceNumber, jint friendNumber, jbyteArray action)
{
    ByteArray action_array(env, action);
    return with_instance(env, instanceNumber, "SendMessage", handle_send_message_error, [](uint32_t message_id) {
        return message_id;
    }, tox_send_action, friendNumber, action_array.data(), action_array.size());
}
