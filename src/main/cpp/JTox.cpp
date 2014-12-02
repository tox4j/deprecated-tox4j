#include <unordered_map>
#include <tox/tox.h>
#include <mutex>
#include <memory>
#include <stdexcept>
#include <utility>

#include "JTox.h"
#include "events.pb.h"

struct Tox4jStruct {
    std::unique_ptr<tox4j::proto::ToxEvents> events;
    std::unique_ptr<std::mutex> mutex;
};

static std::mutex instance_mutex;
static std::unordered_map<Tox*, Tox4jStruct> instance_map;

static inline int throw_tox_killed_exception(JNIEnv *env, char const *message) {
    return env->ThrowNew(env->FindClass("im/tox/tox4j/exceptions/ToxKilledException"), message);
}

template<typename T> T default_value() { return T(); }
template<> void default_value<void>() { }
template<typename Func> auto with_instance (JNIEnv *env, Tox *tox, Func func) -> decltype(func())
{
    instance_mutex.lock();
    Tox4jStruct *instance;
    auto it = instance_map.find(tox);
    if (it != instance_map.end()) {
        instance = &it->second;
    } else {
        instance_mutex.unlock();
        throw_tox_killed_exception(env, "Tox function invoked on killed tox instance!");
        return default_value<decltype(func())>();
    }

    std::lock_guard<std::mutex> lock(*instance->mutex);
    instance_mutex.unlock();
    return func();
}

/*
 * Do setup here. Caching of needed java method IDs etc should be done in this function. It is guaranteed to be called
 * when the library is loaded, and nothing else will be called before this function is called.
 */
jint JNI_OnLoad(JavaVM *, void *) {
    return 0;
}

JNIEXPORT jlong JNICALL Java_im_tox_tox4j_Tox4j_toxNew(JNIEnv *env, jobject, jboolean ipv6enabled, jboolean udpDisabled,
    jboolean proxyEnabled, jstring proxyAddress, jint proxyPort) {
    const char *address = env->GetStringUTFChars(proxyAddress, 0);
    Tox_Options opts;
    opts.ipv6enabled = (uint8_t) ipv6enabled;
    opts.udp_disabled = (uint8_t) udpDisabled;
    if (proxyEnabled) {
        opts.proxy_enabled = true;
        strncpy(opts.proxy_address, address, sizeof(opts.proxy_address) - 1);
        opts.proxy_port = (uint16_t) proxyPort;
    }

    Tox *tox = tox_new(&opts);
    if (tox != nullptr) {
        std::lock_guard<std::mutex> lock(instance_mutex);
        instance_map[tox] = { std::unique_ptr<tox4j::proto::ToxEvents>(new tox4j::proto::ToxEvents()), std::unique_ptr<std::mutex>(new std::mutex()) };
    }
    return (jlong) ((intptr_t) tox);
}

JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_kill(JNIEnv *env, jobject, jlong instance) {
    std::lock_guard<std::mutex> lock(instance_mutex);
    Tox *tox = (Tox*)((intptr_t) instance);
    auto it = instance_map.find(tox);
    if (it == instance_map.end()) {
        throw_tox_killed_exception(env, "kill called on killed tox instance!");
        return;
    }

    Tox4jStruct t(std::move(instance_map[tox]));
    std::lock_guard<std::mutex> ilock(*t.mutex);
    instance_map.erase(it);
    tox_kill(tox);
}

JNIEXPORT jint JNICALL Java_im_tox_tox4j_Tox4j_bootstrap(JNIEnv *env, jobject, jint instance,
    jstring address, jint port, jbyteArray public_key) {
    Tox *tox = (Tox*) ((intptr_t) instance);
    return with_instance(env, tox, [env, tox, address, port, public_key]() {
        const char *_address = env->GetStringUTFChars(address, 0);
        jbyte *_public_key = env->GetByteArrayElements(public_key, 0);

        jint result = tox_bootstrap_from_address(tox, _address, (uint16_t) port, (uint8_t *) public_key);

        env->ReleaseStringUTFChars(address, _address);
        env->ReleaseByteArrayElements(public_key, _public_key, JNI_ABORT);
        return result;
    });
}

JNIEXPORT jint JNICALL Java_im_tox_tox4j_Tox4j_addTcpRelay(JNIEnv *env, jobject, jlong instance, jstring address,
    jint port, jbyteArray public_key) {
    Tox *tox = (Tox *) ((intptr_t) instance);
    return with_instance(env, tox, [env, tox, address, port, public_key]() {
        const char *_address = env->GetStringUTFChars(address, 0);
        jbyte *_public_key = env->GetByteArrayElements(public_key, 0);

        jint result = tox_bootstrap_from_address(tox, _address, (uint16_t) port, (uint8_t *) public_key);

        env->ReleaseStringUTFChars(address, _address);
        env->ReleaseByteArrayElements(public_key, _public_key, JNI_ABORT);
        return result;
    });
}

JNIEXPORT jboolean JNICALL Java_im_tox_tox4j_Tox4j_isConnected(JNIEnv *env, jobject, jlong instance) {
    Tox *tox = (Tox *) ((intptr_t) instance);
    return with_instance(env, tox, [tox]() { return tox_isconnected(tox); });
}

JNIEXPORT jint JNICALL Java_im_tox_tox4j_Tox4j_doInterval(JNIEnv *env, jobject, jlong instance) {
    Tox *tox = (Tox *) ((intptr_t) instance);
    return with_instance(env, tox, [tox]() { return tox_do_interval(tox); });
}

JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_Tox4j_toxDo(JNIEnv *env, jobject, jlong instance) {
    Tox *tox = (Tox *) ((intptr_t) instance);
    return with_instance(env, tox, [tox, env]() {
        tox_do(tox);

        auto placeholder = &instance_map.at(tox);
        int size = placeholder->events->ByteSize();
        char *buffer = new char[size];
        placeholder->events->SerializeToArray(buffer, size);

        jbyteArray jb = env->NewByteArray(size);
        env->SetByteArrayRegion(jb, 0, size, (jbyte *) buffer);

        delete buffer;
        return jb;
    });
}
