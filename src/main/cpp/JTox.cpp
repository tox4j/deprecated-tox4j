#include <unordered_map>
#include <tox/tox.h>
#include <mutex>
#include <memory>
#include <stdexcept>

#include "JTox.h"
#include "events.pb.h"

/*
 * Struct to hold mutex and tox instance. Mutex is automatically constructed.
 */
struct ToxTex {
    ToxTex():mutex(std::shared_ptr<std::mutex>(new std::mutex())),tox(nullptr) {}
    ~ToxTex() {}
    std::shared_ptr<std::mutex> mutex;
    Tox *tox;
};
static std::mutex instance_mutex;
static std::unordered_map<int, ToxTex> instance_map;
static std::unordered_map<Tox*, std::shared_ptr<tox4j::proto::ToxEvents>> events_map;

static inline jint throw_tox_killed_exception(JNIEnv *env, char const *message) {
    jclass exClass;
    char const *className = "im/tox/tox4j/exceptions/ToxKilledException";
    exClass = env->FindClass(className);
    return env->ThrowNew(exClass, message);
}

/*
 * Do setup here. Caching of needed java method IDs etc should be done in this function. It is guaranteed to be called
 * when the library is loaded, and nothing else will be called before this function is called.
 */
jint JNI_OnLoad(JavaVM *, void *) {
    return 0;
}

JNIEXPORT jint JNICALL Java_im_tox_tox4j_Tox4j_toxNew(JNIEnv *env, jobject, jboolean ipv6enabled, jboolean udpDisabled,
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
    int result = -1;
    if (tox != nullptr) {
        std::lock_guard<std::mutex> lock(instance_mutex);
        ToxTex instance;
        instance.tox = tox;
        int max = 0;
        for(auto &it : instance_map) {
            if (it.first >= max) {
                max = it.first + 1;
            }
        }
        result = max;
        instance_map[max] = instance;
        events_map[tox] = std::make_shared<tox4j::proto::ToxEvents>();
    }
    return result;
}

JNIEXPORT jint JNICALL Java_im_tox_tox4j_Tox4j_bootstrap(JNIEnv *env, jobject, jint instance_number,
    jstring address, jint port, jbyteArray public_key) {
    instance_mutex.lock();
    ToxTex instance;
    try {
        instance = instance_map.at(instance_number);
    } catch (const std::out_of_range& oor) {
        instance_mutex.unlock();
        return throw_tox_killed_exception(env, "Bootstrap called on killed tox instance!");
    }

    std::lock_guard<std::mutex> lock(*instance.mutex);
    instance_mutex.unlock();
    const char *_address = env->GetStringUTFChars(address, 0);
    jbyte *_public_key = env->GetByteArrayElements(public_key, 0);

    jint result = tox_bootstrap_from_address(instance.tox, _address, (uint16_t) port, (uint8_t *) public_key);

    env->ReleaseStringUTFChars(address, _address);
    env->ReleaseByteArrayElements(public_key, _public_key, JNI_ABORT);
    return result;
}

JNIEXPORT jint JNICALL Java_im_tox_tox4j_Tox4j_addTcpRelay(JNIEnv *env, jobject, jint instance_number, jstring address,
    jint port, jbyteArray public_key) {
    instance_mutex.lock();
    ToxTex instance;
    try {
        instance = instance_map.at(instance_number);
    } catch (const std::out_of_range& oor) {
        instance_mutex.unlock();
        return throw_tox_killed_exception(env, "AddTcpRelay called on killed tox instance!");
    }

    std::lock_guard<std::mutex> lock(*instance.mutex);
    instance_mutex.unlock();
    const char *_address = env->GetStringUTFChars(address, 0);
    jbyte *_public_key = env->GetByteArrayElements(public_key, 0);

    jint result = tox_add_tcp_relay(instance.tox, _address, (uint16_t) port, (uint8_t *) public_key);

    env->ReleaseStringUTFChars(address, _address);
    env->ReleaseByteArrayElements(public_key, _public_key, JNI_ABORT);
    return result;
}

JNIEXPORT jboolean JNICALL Java_im_tox_tox4j_Tox4j_isConnected(JNIEnv *env, jobject, jint instance_number) {
    instance_mutex.lock();
    ToxTex instance;
    try {
        instance = instance_map.at(instance_number);
    } catch (const std::out_of_range& oor) {
        instance_mutex.unlock();
        return throw_tox_killed_exception(env, "isConnected called on killed tox instance!");
    }

    std::lock_guard<std::mutex> lock(*instance.mutex);
    instance_mutex.unlock();
    return tox_isconnected(instance.tox);
}

JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_kill(JNIEnv *env, jobject, jint instance_number) {
    std::lock_guard<std::mutex> lock(instance_mutex);
    ToxTex instance;
    auto it = instance_map.find(instance_number);
    if (it != instance_map.end()) {
        instance = it->second;
    } else {
        throw_tox_killed_exception(env, "kill called on killed tox instance!");
        return;
    }

    std::lock_guard<std::mutex> ilock(*instance.mutex);
    instance_map.erase(it);
    events_map.erase(instance.tox);
    tox_kill(instance.tox);
}

JNIEXPORT jint JNICALL Java_im_tox_tox4j_Tox4j_doInterval(JNIEnv *env, jobject, jint instance_number) {
    instance_mutex.lock();
    ToxTex instance;
    try {
        instance = instance_map.at(instance_number);
    } catch (const std::out_of_range& oor) {
        instance_mutex.unlock();
        return throw_tox_killed_exception(env, "doInterval called on killed tox instance!");
    }

    std::lock_guard<std::mutex> ilock(*instance.mutex);
    instance_mutex.unlock();
    return tox_do_interval(instance.tox);
}

JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_Tox4j_toxDo(JNIEnv *env, jobject, jint instance_number) {
    instance_mutex.lock();
    ToxTex instance;
    try {
        instance = instance_map.at(instance_number);
    } catch (const std::out_of_range& oor) {
        instance_mutex.unlock();
        throw_tox_killed_exception(env, "do called on killed tox instance!");
        return NULL;
    }

    std::lock_guard<std::mutex> ilock(*instance.mutex);
    instance_mutex.unlock();
    tox_do(instance.tox);

    auto events = events_map.at(instance.tox);
    int size = events->ByteSize();
    char *buffer = new char[size];
    events->SerializeToArray(buffer, size);

    jbyteArray jb = env->NewByteArray(size);
    env->SetByteArrayRegion(jb, 0, size, (jbyte *) buffer);

    delete buffer;
    return jb;
}
