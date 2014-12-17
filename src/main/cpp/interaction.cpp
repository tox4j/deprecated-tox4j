#include "Tox4j.h"


/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxSetTyping
 * Signature: (IIZ)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxSetTyping
  (JNIEnv *env, jclass, jint instanceNumber, jint friendNumber, jboolean isTyping)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(events);
        TOX_ERR_SET_TYPING error;
        tox_set_typing(tox, friendNumber, isTyping, &error);
        switch (error) {
            case TOX_ERR_SET_TYPING_OK:
                return;
            case TOX_ERR_SET_TYPING_FRIEND_NOT_FOUND:
                throw_tox_exception(env, "SetTyping", "FRIEND_NOT_FOUND");
                return;
        }
    });
}

/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxSendMessage
 * Signature: (II[B)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxSendMessage
  (JNIEnv *env, jclass, jint instanceNumber, jint friendNumber, jbyteArray message)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(events);
        TOX_ERR_SEND_MESSAGE error;
        ByteArray message_array(env, message);
        tox_send_message(tox, friendNumber, message_array.data(), message_array.size(), &error);
        switch (error) {
            case TOX_ERR_SEND_MESSAGE_OK:
                return;
            case TOX_ERR_SEND_MESSAGE_NULL:
                throw_tox_exception(env, "SendMessage", "NULL");
                return;
            case TOX_ERR_SEND_MESSAGE_FRIEND_NOT_FOUND:
                throw_tox_exception(env, "SendMessage", "FRIEND_NOT_FOUND");
                return;
            case TOX_ERR_SEND_MESSAGE_SENDQ:
                throw_tox_exception(env, "SendMessage", "SENDQ");
                return;
            case TOX_ERR_SEND_MESSAGE_TOO_LONG:
                throw_tox_exception(env, "SendMessage", "TOO_LONG");
                return;
            case TOX_ERR_SEND_MESSAGE_EMPTY:
                throw_tox_exception(env, "SendMessage", "EMPTY");
                return;
        }
    });
}

/*
 * Class:     im_tox_tox4j_v2_ToxCoreImpl
 * Method:    toxSendAction
 * Signature: (II[B)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_v2_ToxCoreImpl_toxSendAction
  (JNIEnv *env, jclass, jint instanceNumber, jint friendNumber, jbyteArray action)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        unused(events);
        TOX_ERR_SEND_MESSAGE error;
        ByteArray action_array(env, action);
        tox_send_action(tox, friendNumber, action_array.data(), action_array.size(), &error);
        switch (error) {
            case TOX_ERR_SEND_MESSAGE_OK:
                return;
            case TOX_ERR_SEND_MESSAGE_NULL:
                throw_tox_exception(env, "SendMessage", "NULL");
                return;
            case TOX_ERR_SEND_MESSAGE_FRIEND_NOT_FOUND:
                throw_tox_exception(env, "SendMessage", "FRIEND_NOT_FOUND");
                return;
            case TOX_ERR_SEND_MESSAGE_SENDQ:
                throw_tox_exception(env, "SendMessage", "SENDQ");
                return;
            case TOX_ERR_SEND_MESSAGE_TOO_LONG:
                throw_tox_exception(env, "SendMessage", "TOO_LONG");
                return;
            case TOX_ERR_SEND_MESSAGE_EMPTY:
                throw_tox_exception(env, "SendMessage", "EMPTY");
                return;
        }
    });
}
