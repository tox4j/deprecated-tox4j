#include <vector>
#include <tox/tox.h>
#include <mutex>
#include <memory>
#include <stdexcept>
#include <utility>

#include "JTox.h"
#include "events.pb.h"

struct UTFChars {
    UTFChars(JNIEnv *env, jstring string)
    : env(env)
    , string(string)
    , chars(env->GetStringUTFChars(string, 0)) { }

    UTFChars(UTFChars const &) = delete;
    ~UTFChars() { env->ReleaseStringUTFChars(string, chars); }

    operator char const *() const { return chars; }

private:
    JNIEnv *env;
    jstring string;
    char const *chars;
};

struct ByteArray {
    ByteArray(JNIEnv *env, jbyteArray byteArray)
    : env(env)
    , byteArray(byteArray)
    , bytes(env->GetByteArrayElements(byteArray, 0)) { }

    ByteArray(ByteArray const &) = delete;
    ~ByteArray() { env->ReleaseByteArrayElements(byteArray, bytes, JNI_ABORT); }

    operator uint8_t const *() const { return (uint8_t *)bytes; }

private:
    JNIEnv *env;
    jbyteArray byteArray;
    jbyte *bytes;
};

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

template<typename Func>
auto with_instance(JNIEnv *env, jint instance_number, Func func)
    -> decltype(func(std::declval<Tox *>(), std::declval<tox4j::proto::ToxEvents &>()))
{
    typedef decltype(func(std::declval<Tox *>(), std::declval<tox4j::proto::ToxEvents &>())) return_type;

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

JNIEXPORT jint JNICALL Java_im_tox_tox4j_Tox4j_toxNew(JNIEnv *env, jclass, jboolean ipv6enabled, jboolean udpDisabled,
    jboolean proxyEnabled, jstring proxyAddress, jint proxyPort) {
    auto opts = Tox_Options();
    opts.ipv6enabled = (uint8_t) ipv6enabled;
    opts.udp_disabled = (uint8_t) udpDisabled;
    if (proxyEnabled) {
        opts.proxy_enabled = true;
        strncpy(opts.proxy_address, UTFChars(env, proxyAddress), sizeof(opts.proxy_address) - 1);
        opts.proxy_port = (uint16_t) proxyPort;
    }

    std::unique_ptr<Tox, ToxDeleter> tox(tox_new(&opts));
    if (tox == nullptr) {
        return -1;
    }

    std::lock_guard<std::mutex> lock(instance_mutex);
    instance_vector.push_back({
        std::move(tox),
        std::unique_ptr<tox4j::proto::ToxEvents>(new tox4j::proto::ToxEvents),
        std::unique_ptr<std::mutex>(new std::mutex)
    });
    return (jint) (instance_vector.size() - 1);
}

JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_kill(JNIEnv *env, jclass, jint instance_number) {
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

JNIEXPORT jint JNICALL Java_im_tox_tox4j_Tox4j_bootstrap(JNIEnv *env, jclass, jint instance_number,
    jstring address, jint port, jbyteArray public_key) {
    return with_instance(env, instance_number, [=](Tox *tox, tox4j::proto::ToxEvents) {
        return tox_bootstrap_from_address(tox, UTFChars(env, address), (uint16_t) port, ByteArray(env, public_key));
    });
}

JNIEXPORT jint JNICALL Java_im_tox_tox4j_Tox4j_addTcpRelay(JNIEnv *env, jclass, jint instance_number, jstring address,
    jint port, jbyteArray public_key) {
    return with_instance(env, instance_number, [=](Tox *tox, tox4j::proto::ToxEvents) {
        return tox_add_tcp_relay(tox, UTFChars(env, address), (uint16_t) port, ByteArray(env, public_key));
    });
}

JNIEXPORT jboolean JNICALL Java_im_tox_tox4j_Tox4j_isConnected(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, tox4j::proto::ToxEvents) { return tox_isconnected(tox); });
}

JNIEXPORT jint JNICALL Java_im_tox_tox4j_Tox4j_doInterval(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, tox4j::proto::ToxEvents) { return tox_do_interval(tox); });
}

JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_Tox4j_toxDo(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, tox4j::proto::ToxEvents events) {
        tox_do(tox);

        std::vector<char> buffer(events.ByteSize());
        events.SerializeToArray(buffer.data(), buffer.size());

        jbyteArray jb = env->NewByteArray(buffer.size());
        env->SetByteArrayRegion(jb, 0, buffer.size(), (jbyte *)buffer.data());
        events.Clear();
        return jb;
    });
}
