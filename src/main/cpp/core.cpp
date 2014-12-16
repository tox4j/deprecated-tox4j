#include <tox/core.h>
#include "Tox4j.h"

template<typename T> void unused(T const &) { }


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_options_default(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_options_default);
        throw_unsupported_operation_exception(env, instance_number, "tox_options_default");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_options_new(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_options_new);
        throw_unsupported_operation_exception(env, instance_number, "tox_options_new");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_options_free(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_options_free);
        throw_unsupported_operation_exception(env, instance_number, "tox_options_free");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_new(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_new);
        throw_unsupported_operation_exception(env, instance_number, "tox_new");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_kill(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_kill);
        throw_unsupported_operation_exception(env, instance_number, "tox_kill");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_save_size(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_save_size);
        throw_unsupported_operation_exception(env, instance_number, "tox_save_size");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_save(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_save);
        throw_unsupported_operation_exception(env, instance_number, "tox_save");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_load(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_load);
        throw_unsupported_operation_exception(env, instance_number, "tox_load");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_bootstrap(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_bootstrap);
        throw_unsupported_operation_exception(env, instance_number, "tox_bootstrap");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_is_connected(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_is_connected);
        throw_unsupported_operation_exception(env, instance_number, "tox_is_connected");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_callback_connection_status(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_callback_connection_status);
        throw_unsupported_operation_exception(env, instance_number, "tox_callback_connection_status");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_get_port(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_get_port);
        throw_unsupported_operation_exception(env, instance_number, "tox_get_port");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_do_interval(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_do_interval);
        throw_unsupported_operation_exception(env, instance_number, "tox_do_interval");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_do(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_do);
        throw_unsupported_operation_exception(env, instance_number, "tox_do");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_get_self_address(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_get_self_address);
        throw_unsupported_operation_exception(env, instance_number, "tox_get_self_address");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_set_nospam(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_set_nospam);
        throw_unsupported_operation_exception(env, instance_number, "tox_set_nospam");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_get_nospam(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_get_nospam);
        throw_unsupported_operation_exception(env, instance_number, "tox_get_nospam");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_get_self_client_id(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_get_self_client_id);
        throw_unsupported_operation_exception(env, instance_number, "tox_get_self_client_id");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_get_secret_key(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_get_secret_key);
        throw_unsupported_operation_exception(env, instance_number, "tox_get_secret_key");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_set_self_name(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_set_self_name);
        throw_unsupported_operation_exception(env, instance_number, "tox_set_self_name");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_self_name_size(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_self_name_size);
        throw_unsupported_operation_exception(env, instance_number, "tox_self_name_size");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_get_self_name(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_get_self_name);
        throw_unsupported_operation_exception(env, instance_number, "tox_get_self_name");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_set_self_status_message(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_set_self_status_message);
        throw_unsupported_operation_exception(env, instance_number, "tox_set_self_status_message");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_self_status_message_size(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_self_status_message_size);
        throw_unsupported_operation_exception(env, instance_number, "tox_self_status_message_size");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_get_self_status_message(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_get_self_status_message);
        throw_unsupported_operation_exception(env, instance_number, "tox_get_self_status_message");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_set_self_status(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_set_self_status);
        throw_unsupported_operation_exception(env, instance_number, "tox_set_self_status");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_get_self_status(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_get_self_status);
        throw_unsupported_operation_exception(env, instance_number, "tox_get_self_status");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_add_friend(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_add_friend);
        throw_unsupported_operation_exception(env, instance_number, "tox_add_friend");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_add_friend_norequest(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_add_friend_norequest);
        throw_unsupported_operation_exception(env, instance_number, "tox_add_friend_norequest");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_delete_friend(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_delete_friend);
        throw_unsupported_operation_exception(env, instance_number, "tox_delete_friend");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_get_friend_number(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_get_friend_number);
        throw_unsupported_operation_exception(env, instance_number, "tox_get_friend_number");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_get_friend_client_id(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_get_friend_client_id);
        throw_unsupported_operation_exception(env, instance_number, "tox_get_friend_client_id");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_friend_exists(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_friend_exists);
        throw_unsupported_operation_exception(env, instance_number, "tox_friend_exists");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_friend_list_size(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_friend_list_size);
        throw_unsupported_operation_exception(env, instance_number, "tox_friend_list_size");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_get_friend_list(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_get_friend_list);
        throw_unsupported_operation_exception(env, instance_number, "tox_get_friend_list");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_get_friend_name_size(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_get_friend_name_size);
        throw_unsupported_operation_exception(env, instance_number, "tox_get_friend_name_size");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_get_friend_name(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_get_friend_name);
        throw_unsupported_operation_exception(env, instance_number, "tox_get_friend_name");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_callback_friend_name(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_callback_friend_name);
        throw_unsupported_operation_exception(env, instance_number, "tox_callback_friend_name");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_get_friend_status_message_size(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_get_friend_status_message_size);
        throw_unsupported_operation_exception(env, instance_number, "tox_get_friend_status_message_size");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_get_friend_status_message(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_get_friend_status_message);
        throw_unsupported_operation_exception(env, instance_number, "tox_get_friend_status_message");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_callback_friend_status_message(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_callback_friend_status_message);
        throw_unsupported_operation_exception(env, instance_number, "tox_callback_friend_status_message");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_get_friend_status(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_get_friend_status);
        throw_unsupported_operation_exception(env, instance_number, "tox_get_friend_status");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_callback_friend_status(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_callback_friend_status);
        throw_unsupported_operation_exception(env, instance_number, "tox_callback_friend_status");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_get_friend_is_connected(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_get_friend_is_connected);
        throw_unsupported_operation_exception(env, instance_number, "tox_get_friend_is_connected");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_callback_friend_connected(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_callback_friend_connected);
        throw_unsupported_operation_exception(env, instance_number, "tox_callback_friend_connected");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_get_friend_is_typing(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_get_friend_is_typing);
        throw_unsupported_operation_exception(env, instance_number, "tox_get_friend_is_typing");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_callback_friend_typing(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_callback_friend_typing);
        throw_unsupported_operation_exception(env, instance_number, "tox_callback_friend_typing");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_set_typing(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_set_typing);
        throw_unsupported_operation_exception(env, instance_number, "tox_set_typing");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_send_message(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_send_message);
        throw_unsupported_operation_exception(env, instance_number, "tox_send_message");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_send_action(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_send_action);
        throw_unsupported_operation_exception(env, instance_number, "tox_send_action");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_callback_read_receipt(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_callback_read_receipt);
        throw_unsupported_operation_exception(env, instance_number, "tox_callback_read_receipt");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_callback_friend_request(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_callback_friend_request);
        throw_unsupported_operation_exception(env, instance_number, "tox_callback_friend_request");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_callback_friend_message(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_callback_friend_message);
        throw_unsupported_operation_exception(env, instance_number, "tox_callback_friend_message");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_callback_friend_action(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_callback_friend_action);
        throw_unsupported_operation_exception(env, instance_number, "tox_callback_friend_action");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_hash(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_hash);
        throw_unsupported_operation_exception(env, instance_number, "tox_hash");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_file_control(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_file_control);
        throw_unsupported_operation_exception(env, instance_number, "tox_file_control");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_callback_file_control(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_callback_file_control);
        throw_unsupported_operation_exception(env, instance_number, "tox_callback_file_control");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_file_send(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_file_send);
        throw_unsupported_operation_exception(env, instance_number, "tox_file_send");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_callback_file_send_chunk(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_callback_file_send_chunk);
        throw_unsupported_operation_exception(env, instance_number, "tox_callback_file_send_chunk");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_callback_file_recv(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_callback_file_recv);
        throw_unsupported_operation_exception(env, instance_number, "tox_callback_file_recv");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_callback_file_recv_chunk(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_callback_file_recv_chunk);
        throw_unsupported_operation_exception(env, instance_number, "tox_callback_file_recv_chunk");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_send_lossy_packet(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_send_lossy_packet);
        throw_unsupported_operation_exception(env, instance_number, "tox_send_lossy_packet");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_callback_lossy_packet(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_callback_lossy_packet);
        throw_unsupported_operation_exception(env, instance_number, "tox_callback_lossy_packet");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_send_lossless_packet(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_send_lossless_packet);
        throw_unsupported_operation_exception(env, instance_number, "tox_send_lossless_packet");
    });
}


JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_tox_callback_lossless_packet(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        unused(tox);
        unused(events);
        unused(tox_callback_lossless_packet);
        throw_unsupported_operation_exception(env, instance_number, "tox_callback_lossless_packet");
    });
}
