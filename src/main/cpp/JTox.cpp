#include <unordered_map>
#include <tox/tox.h>
#include <mutex>
#include <memory>
#include <stdexcept>
#include <utility>
#include <vector>

#include "JTox.h"
#include "events.pb.h"


struct Tox4jStruct {
    Tox *tox;
    tox4j::proto::ToxEvents events;
    std::mutex mutex;
};

struct ToxKilledException : std::runtime_error {
    ToxKilledException(const std::string& msg = "") : std::runtime_error(msg)
    {}
};

static std::mutex instance_mutex;
static std::vector<std::shared_ptr<Tox4jStruct>> instance_vector;
static uint64_t instance_count = 0;

class ToxLock
{
public:
    ToxLock(jlong instance)
    {
        std::lock_guard<std::mutex> glock(instance_mutex);

        // 0 is used as to indicate an error to java, so IDs are one-based

        if(instance <= 0 || instance > (jlong)instance_vector.size())
            throw ToxKilledException("Instance ID out of range");

        inst = instance_vector.at(instance - 1);

        if(!inst)
            throw ToxKilledException("Instance was already killed");

        inst->mutex.lock();
    }

    ~ToxLock()
    {
        inst->mutex.unlock();
    }

    inline Tox *getTox()
    {
        return inst->tox;
    }

    inline const std::shared_ptr<Tox4jStruct> &getInst()
    {
        return inst;
    }

private:
    std::shared_ptr<Tox4jStruct> inst;
};

static inline void throw_java_exception(JNIEnv *env, const char *type, char const *message)
{
    jclass excClass = env->FindClass(type);

    if(excClass)
        env->ThrowNew(excClass, message);
}

static inline void translate_exception(JNIEnv *env)
{
    try {
        throw;
    } catch(const ToxKilledException &e) {
        std::string msg = "Tox was already killed for this instance";

        if(e.what() && *e.what()) {
            msg += ": ";
            msg += e.what();
        }

        throw_java_exception(env, "im/tox/tox4j/exceptions/ToxKilledException", msg.c_str());
    } catch(const std::exception &e) {
        throw_java_exception(env, "java/lang/Error", e.what());
    } catch(const char *e) {
        throw_java_exception(env, "java/lang/Error", e);
    } catch(...) {
        throw_java_exception(env, "java/lang/Error", "Unknown C++ exception occured");
    }
}

/*
 * Do setup here. Caching of needed java method IDs etc should be done in this function. It is guaranteed to be called
 * when the library is loaded, and nothing else will be called before this function is called.
 */
jint JNI_OnLoad(JavaVM *, void *)
{
    return 0;
}

JNIEXPORT jlong JNICALL Java_im_tox_tox4j_Tox4j_toxNew(JNIEnv *env, jobject, jboolean ipv6enabled, jboolean udpDisabled,
        jboolean proxyEnabled, jstring proxyAddress, jint proxyPort)
{
    try {
        const char *address = env->GetStringUTFChars(proxyAddress, 0);
        Tox_Options opts;
        opts.ipv6enabled = (uint8_t) ipv6enabled;
        opts.udp_disabled = (uint8_t) udpDisabled;
        if(proxyEnabled) {
            opts.proxy_enabled = true;
            strncpy(opts.proxy_address, address, sizeof(opts.proxy_address) - 1);
            opts.proxy_port = (uint16_t) proxyPort;
        }

        Tox *tox = tox_new(&opts);
        if(tox != nullptr) {
            std::lock_guard<std::mutex> lock(instance_mutex);

            instance_vector.push_back(std::make_shared<Tox4jStruct>());
            instance_count += 1;

            // 0 is used as to indicate an error to java, so IDs are one-based
            return (jlong)instance_vector.size();
        }
    } catch(...) {
        translate_exception(env);
    }

    return 0;
}

JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_kill(JNIEnv *env, jobject, jlong instance)
{
    try {
        ToxLock tlock(instance);
        std::lock_guard<std::mutex> glock(instance_mutex);

        if(instance_count == 0)
            throw std::runtime_error("Instance counter corrupted");

        // 0 is used as to indicate an error to java, so IDs are one-based

        if(instance > (jlong)instance_vector.size())
            throw std::runtime_error("Concurrent deletion of same Tox");

        if(instance == (jlong)instance_vector.size())
            instance_vector.pop_back();
        else
            instance_vector.at(instance - 1).reset();

        tox_kill(tlock.getTox());

        instance_count -= 1;

        if(instance_count == 0)
            instance_vector.clear();
    } catch(...) {
        translate_exception(env);
    }
}

JNIEXPORT jint JNICALL Java_im_tox_tox4j_Tox4j_bootstrap(JNIEnv *env, jobject, jlong instance,
        jstring address, jint port, jbyteArray public_key)
{
    try {
        ToxLock tlock(instance);

        const char *_address = env->GetStringUTFChars(address, 0);
        jbyte *_public_key = env->GetByteArrayElements(public_key, 0);

        jint result = tox_bootstrap_from_address(tlock.getTox(), _address, (uint16_t)port, (uint8_t *)public_key);

        env->ReleaseStringUTFChars(address, _address);
        env->ReleaseByteArrayElements(public_key, _public_key, JNI_ABORT);
        return result;
    } catch(...) {
        translate_exception(env);
        return 0;
    }
}

JNIEXPORT jint JNICALL Java_im_tox_tox4j_Tox4j_addTcpRelay(JNIEnv *env, jobject, jlong instance, jstring address,
        jint port, jbyteArray public_key)
{
    try {
        ToxLock tlock(instance);

        const char *_address = env->GetStringUTFChars(address, 0);
        jbyte *_public_key = env->GetByteArrayElements(public_key, 0);

        jint result = tox_bootstrap_from_address(tlock.getTox(), _address, (uint16_t) port, (uint8_t *) public_key);

        env->ReleaseStringUTFChars(address, _address);
        env->ReleaseByteArrayElements(public_key, _public_key, JNI_ABORT);
        return result;
    } catch(...) {
        translate_exception(env);
        return 0;
    }
}

JNIEXPORT jboolean JNICALL Java_im_tox_tox4j_Tox4j_isConnected(JNIEnv *env, jobject, jlong instance)
{
    try {
        ToxLock tlock(instance);

        return tox_isconnected(tlock.getTox());
    } catch(...) {
        translate_exception(env);
        return 0;
    }
}

JNIEXPORT jint JNICALL Java_im_tox_tox4j_Tox4j_doInterval(JNIEnv *env, jobject, jlong instance)
{
    try {
        ToxLock tlock(instance);

        return tox_do_interval(tlock.getTox());
    } catch(...) {
        translate_exception(env);
        return 0;
    }
}

JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_Tox4j_toxDo(JNIEnv *env, jobject, jlong instance)
{
    try {
        ToxLock tlock(instance);

        tox_do(tlock.getTox());

        std::vector<char> buffer(tlock.getInst()->events.ByteSize());
        tlock.getInst()->events.SerializeToArray(buffer.data(), buffer.size());

        jbyteArray jb = env->NewByteArray(buffer.size());
        env->SetByteArrayRegion(jb, 0, buffer.size(), (jbyte*)buffer.data());

        return jb;
    } catch(...) {
        translate_exception(env);
        return 0;
    }
}
