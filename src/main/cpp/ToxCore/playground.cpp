#include "tox4j/Tox4j.h"
#include "jniutil.h"

/*
 * Class:     im_tox_tox4j_ToxCoreImpl
 * Method:    playground
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxCoreImpl_playground
  (JNIEnv *env, jclass, jint instance_number)
{
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &) {
        uint8_t name[TOX_MAX_NAME_LENGTH] = { 0 };
        tox_self_set_name(tox, name, sizeof name, NULL);
        assert(tox_self_get_name_size(tox) == sizeof name);

        size_t save_size = tox_save_size(tox);
        uint8_t *data = new uint8_t[save_size];
        tox_save(tox, data);

        Tox *tox2 = tox_new(NULL, data, save_size, NULL);

        size_t length = tox_self_get_name_size(tox2);
        printf("new length: %zd\n", length);
        assert(tox_self_get_name_size(tox2) == sizeof name);

        uint8_t new_name[128] = { 0 };
        tox_self_get_name(tox2, new_name);
        assert(memcmp(name, new_name, 128) == 0);
    });
}
