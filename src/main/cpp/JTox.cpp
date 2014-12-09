#include <vector>
#include <tox/tox.h>
#include <mutex>
#include <memory>
#include <stdexcept>
#include <utility>

#include "JTox.h"
#include "events.pb.h"

struct ToxDeleter {
    void operator()(Tox *tox) {
        tox_kill(tox);
    }
};

struct Tox4jStruct {
    std::unique_ptr<Tox, ToxDeleter> tox;
    std::unique_ptr<tox4j::proto::ToxEvents> events;
    std::unique_ptr<std::mutex> mutex;
};

static std::mutex instance_mutex;
static std::vector<Tox4jStruct> instance_vector;

static inline void throw_tox_killed_exception(JNIEnv *env, char const *message) {
    env->ThrowNew(env->FindClass("im/tox/tox4j/exceptions/ToxKilledException"), message);
}

static inline void throw_illegal_state_exception(JNIEnv *env, char const *message) {
    env->ThrowNew(env->FindClass("java/lang/IllegalStateException"), message);
}

template<typename T> T default_value() { return T(); }
template<> void default_value<void>() { }
template<typename Func> auto with_instance (JNIEnv *env, jint instance_number, Func func) -> decltype(func(std::declval<Tox*>(), std::declval<tox4j::proto::ToxEvents&>()))
{
    typedef decltype(func(std::declval<Tox*>(), std::declval<tox4j::proto::ToxEvents&>())) return_type;
    instance_mutex.lock();
    if (instance_number < 0) {
        throw_illegal_state_exception(env, "Tox instance out of range");
        return default_value<return_type>();
    }

    if (instance_number >= instance_vector.size()) {
        throw_tox_killed_exception(env, "Tox function invoked on killed tox instance!");
        return default_value<return_type>();
    }

    Tox4jStruct &instance = instance_vector.at(instance_number);

    std::lock_guard<std::mutex> lock(*instance.mutex);
    auto tox = instance.tox.get();
    auto events = *instance.events;
    instance_mutex.unlock();
    return func(tox, events);
}

/*
 * Do setup here. Caching of needed java method IDs etc should be done in this function. It is guaranteed to be called
 * when the library is loaded, and nothing else will be called before this function is called.
 */
jint JNI_OnLoad(JavaVM *, void *) {
    return JNI_VERSION_1_4;
}

JNIEXPORT jint JNICALL Java_im_tox_tox4j_Tox4j_toxNew(JNIEnv *env, jobject, jboolean ipv6enabled, jboolean udpDisabled,
    jboolean proxyEnabled, jstring proxyAddress, jint proxyPort) {
    Tox_Options opts;
    opts.ipv6enabled = (uint8_t) ipv6enabled;
    opts.udp_disabled = (uint8_t) udpDisabled;
    if (proxyEnabled) {
        opts.proxy_enabled = true;
        const char *address = env->GetStringUTFChars(proxyAddress, 0);
        strncpy(opts.proxy_address, address, sizeof(opts.proxy_address) - 1);
        env->ReleaseStringUTFChars(proxyAddress, address);
        opts.proxy_port = (uint16_t) proxyPort;
    }

    Tox *tox = tox_new(&opts);
    if (tox == nullptr) {
        return -1;
    }

    std::lock_guard<std::mutex> lock(instance_mutex);
    instance_vector.push_back({ std::unique_ptr<Tox, ToxDeleter>(tox), std::unique_ptr<tox4j::proto::ToxEvents>(), std::unique_ptr<std::mutex>() });
    return (jint) (instance_vector.size() - 1);
}

JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_kill(JNIEnv *env, jobject, jint instance_number) {
    std::lock_guard<std::mutex> lock(instance_mutex);

    if (instance_number < 0) {
        throw_illegal_state_exception(env, "Tox instance out of range");
        return;
    }
    if (instance_number >= (jint) instance_vector.size()) {
        throw_tox_killed_exception(env, "kill called on already killed/nonexistant instance");
        return;
    }
    Tox4jStruct t(std::move(instance_vector[instance_number]));

    if (!t.tox) {
        throw_tox_killed_exception(env, "kill called on already killed instance");
        return;
    }

    std::lock_guard<std::mutex> ilock(*t.mutex);
    if (instance_number == (jint) (instance_vector.size() - 1)) {
        instance_vector.pop_back();
    }
}

JNIEXPORT jint JNICALL Java_im_tox_tox4j_Tox4j_bootstrap(JNIEnv *env, jobject, jint instance_number,
    jstring address, jint port, jbyteArray public_key) {
    return with_instance(env, instance_number, [=](Tox *tox, tox4j::proto::ToxEvents) {
        const char *_address = env->GetStringUTFChars(address, 0);
        jbyte *_public_key = env->GetByteArrayElements(public_key, 0);

        jint result = tox_bootstrap_from_address(tox, _address, (uint16_t) port, (uint8_t *) public_key);

        env->ReleaseStringUTFChars(address, _address);
        env->ReleaseByteArrayElements(public_key, _public_key, JNI_ABORT);
        return result;
    });
}

JNIEXPORT jint JNICALL Java_im_tox_tox4j_Tox4j_addTcpRelay(JNIEnv *env, jobject, jint instance_number, jstring address,
    jint port, jbyteArray public_key) {
    return with_instance(env, instance_number, [=](Tox *tox, tox4j::proto::ToxEvents) {
        const char *_address = env->GetStringUTFChars(address, 0);
        jbyte *_public_key = env->GetByteArrayElements(public_key, 0);

        jint result = tox_add_tcp_relay(tox, _address, (uint16_t) port, (uint8_t *) public_key);

        env->ReleaseStringUTFChars(address, _address);
        env->ReleaseByteArrayElements(public_key, _public_key, JNI_ABORT);
        return result;
    });
}

JNIEXPORT jboolean JNICALL Java_im_tox_tox4j_Tox4j_isConnected(JNIEnv *env, jobject, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, tox4j::proto::ToxEvents) { return tox_isconnected(tox); });
}

JNIEXPORT jint JNICALL Java_im_tox_tox4j_Tox4j_doInterval(JNIEnv *env, jobject, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, tox4j::proto::ToxEvents) { return tox_do_interval(tox); });
}

JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_Tox4j_toxDo(JNIEnv *env, jobject, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, tox4j::proto::ToxEvents events) {
        tox_do(tox);

        std::vector<char> buffer(events.ByteSize());
        events.SerializeToArray(buffer.data(), buffer.size());

        jbyteArray jb = env->NewByteArray(buffer.size());
        env->SetByteArrayRegion(jb, 0, buffer.size(), (jbyte*)buffer.data());

        return jb;
    });
}
