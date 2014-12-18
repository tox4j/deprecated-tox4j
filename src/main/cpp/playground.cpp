#include "tox4j/Tox4j.h"
#include "jniutil.h"

static uint16_t compute_checksum(uint8_t const *address) {
    uint16_t checksum = 0;

    for (size_t i = 0; i < (TOX_ADDRESS_SIZE - 2) / 2; i++)
        checksum ^= reinterpret_cast<uint16_t const *>(address)[i];

    return checksum;
}

JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_playground(JNIEnv *env, jclass, jint instance_number) {
    if (!getenv("TOX4J_ENABLE_PLAYGROUND"))
        return;

    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &) {
        uint8_t address[TOX_ADDRESS_SIZE] = { 0 };
        uint8_t data[] = { 'a' };
        for (uint32_t i = 0; i < 4294967295; i++) {
            *reinterpret_cast<uint32_t *>(address) = i;
            *reinterpret_cast<uint16_t *>(address + TOX_ADDRESS_SIZE - 2) = compute_checksum(address);
            int32_t friend_id = tox_friend_add(tox, address, data, 1, NULL);
            if (i % 1000 == 0) {
                printf("add friend: %d\n", friend_id);
            }
            //tox_del_friend(tox, friend_id);
        }
    });
}
