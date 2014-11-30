#include <unordered_map>
#include <tox/tox.h>
#include <mutex>

#include "JTox.h"
#include "events.pb.h"

static std::mutex instance_mutex;
static std::unordered_map<int, Tox*> instance_map;
static std::mutex events_mutex;
static tox4j::proto::ToxEvents events;

static inline Tox* get_tox_instance(int instance_number) {
    Tox *instance = nullptr;
    std::lock_guard<std::mutex> lock(instance_mutex);
    auto it = instance_map.find(instance_number);
    if (it != instance_map.end()) {
        instance = it->second;
    }
    return instance;
}

/*
 * Do setup here. Caching of needed java method IDs etc should be done in this function. It is guaranteed to be called
 * when the library is loaded, and nothing else will be called before this function is called.
 */
jint JNI_OnLoad(JavaVM *vm, void *reserved) {

}

JNIEXPORT jint JNICALL Java_im_tox_tox4j_Tox4j_toxNew(JNIEnv *env, jobject, jboolean ipv6enabled, jboolean udpDisabled,
    jboolean proxyEnabled, jstring proxyAddress, jint proxyPort) {
    const char* address = env->GetStringUTFChars(proxyAddress, 0);
    Tox_Options opts;
    opts.ipv6enabled = (uint8_t) ipv6enabled;
    opts.udp_disabled = (uint8_t) udpDisabled;
    if (proxyEnabled) {
        opts.proxy_enabled = true;
        strncpy(opts.proxy_address, address, sizeof(opts.proxy_address) - 1);
        opts.proxy_port = (uint16_t) proxyPort;
    }

    Tox *tox = tox_new(&opts);
    int result = -1;
    if (tox != nullptr) {
        std::lock_guard<std::mutex> lock(instance_mutex);
        int max = 0;
        for(auto &it : instance_map) {
            if (it.first >= max) {
                max = it.first + 1;
            }
        }
        result = max;
        std::pair<int, Tox*> newpair(max, tox);
        instance_map.insert(newpair);
    }
    return result;
}

JNIEXPORT jint JNICALL Java_im_tox_tox4j_Tox4j_bootstrap(JNIEnv *env, jobject, jint instance_number,
    jstring address, jint port, jbyteArray public_key) {
    jint result = 0;
    Tox *tox = get_tox_instance(instance_number);

    if (tox != nullptr) {
        const char* _address = env->GetStringUTFChars(address, 0);
        jbyte *_public_key = env->GetByteArrayElements(public_key, 0);

        result = tox_bootstrap_from_address(tox, _address, (uint16_t) port, (uint8_t *) public_key);

        env->ReleaseStringUTFChars(address, _address);
        env->ReleaseByteArrayElements(public_key, _public_key, JNI_ABORT);
    }
    return result;
}

JNIEXPORT jint JNICALL Java_im_tox_tox4j_Tox4j_addTcpRelay(JNIEnv *env, jobject, jint instance_number, jstring address,
    jint port, jbyteArray public_key) {
    jint result = 0;
    Tox *tox = get_tox_instance(instance_number);

    if (tox != nullptr) {
        const char* _address = env->GetStringUTFChars(address, 0);
        jbyte *_public_key = env->GetByteArrayElements(public_key, 0);

        result = tox_add_tcp_relay(tox, _address, (uint16_t) port, (uint8_t *) public_key);

        env->ReleaseStringUTFChars(address, _address);
        env->ReleaseByteArrayElements(public_key, _public_key, JNI_ABORT);
    }
}

JNIEXPORT jboolean JNICALL Java_im_tox_tox4j_Tox4j_isConnected(JNIEnv *, jobject, jint instance_number) {
    Tox *tox = get_tox_instance(instance_number);

    jboolean connected = false;
    if (tox != nullptr) {
        connected = tox_isconnected(tox);
    }
    return connected;
}

JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_kill(JNIEnv *, jobject, jint instance_number) {
    Tox *tox = get_tox_instance(instance_number);

    if (tox != nullptr) {
        tox_kill(tox);
    }
}

JNIEXPORT jint JNICALL Java_im_tox_tox4j_Tox4j_doInterval(JNIEnv *, jobject, jint instance_number) {
    Tox *tox = get_tox_instance(instance_number);

    jint interval = 0;
    if (tox != nullptr) {
        interval = tox_do_interval(tox);
    }
    return interval;
}

JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_Tox4j_toxDo(JNIEnv *env, jobject, jint instance_number) {
    Tox *tox = get_tox_instance(instance_number);

    char *buffer = nullptr;
    jbyteArray jb = nullptr;

    if (tox != nullptr) {
        tox_do(tox);
        std::lock_guard<std::mutex> lock(events_mutex);
        int size = events.ByteSize();
        buffer = new char[size];
        events.SerializeToArray(buffer, size);
        events.Clear();

        jb = env->NewByteArray(size);
        env->SetByteArrayRegion(jb, 0, size, (jbyte *) buffer);
    }

    if (jb == nullptr) {
        jb = env->NewByteArray(0);
    }
    return jb;
}
