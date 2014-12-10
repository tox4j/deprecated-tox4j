#include <algorithm>
#include <vector>
#include <tox/tox.h>
#include <mutex>
#include <memory>
#include <sstream>
#include <stdexcept>
#include <utility>

#include "JTox.h"
#include "jniutil.h"
#include "events.pb.h"

using tox4j::proto::ToxEvents;

struct ToxDeleter {
    void operator()(Tox *tox) {
        tox_kill(tox);
    }
};

class Tox4jStruct {
    // Objects of this class can conceptionally have three states:
    // - LIVE: A tox instance is alive and running.
    // - DEAD: The object is empty, all pointers are nullptr.
    // - COLLECTED: state is DEAD, and the object's instance_number is in instance_freelist.
    bool live = true;

public:
    std::unique_ptr<Tox, ToxDeleter> tox;
    std::unique_ptr<ToxEvents> events;
    std::unique_ptr<std::mutex> mutex;

    bool isLive() const { return live; }
    bool isDead() const { return !live; }

    Tox4jStruct(std::unique_ptr<Tox, ToxDeleter> &&tox,
                std::unique_ptr<ToxEvents> &&events,
                std::unique_ptr<std::mutex> &&mutex)
    : tox(std::move(tox))
    , events(std::move(events))
    , mutex(std::move(mutex))
    { }

    // Move members from another object into this new one, then set the old one to the DEAD state.
    Tox4jStruct(Tox4jStruct &&rhs)
    : tox(std::move(rhs.tox))
    , events(std::move(rhs.events))
    , mutex(std::move(rhs.mutex))
    { rhs.live = false; }

    // Move members from another object into this existing one, then set the right hand side to the DEAD state.
    // This object is then live again.
    Tox4jStruct &operator=(Tox4jStruct &&rhs) {
        assert(!live);

        tox = std::move(rhs.tox);
        events = std::move(rhs.events);
        mutex = std::move(rhs.mutex);
        rhs.live = false;
        this->live = true;

        return *this;
    }
};
// This struct should remain small. Check some assumptions here.
static_assert(sizeof(Tox4jStruct) == sizeof(void *) * 4,
    "Tox4jStruct has unexpected members or padding");

class ToxInstances {
    std::vector<Tox4jStruct> instances;
    std::vector<jint> freelist;

public:
    std::mutex mutex;

    bool isValid(jint instance_number) const {
        return instance_number > 0
            && (size_t) instance_number <= instances.size();
    }

    bool isFree(jint instance_number) const {
        return std::find(freelist.begin(), freelist.end(), instance_number) != freelist.end();
    }

    void setFree(jint instance_number) {
        freelist.push_back(instance_number);
    }

    Tox4jStruct const &operator[](jint instance_number) const {
        return instances[instance_number - 1];
    }

    jint add(Tox4jStruct &&instance) {
        // If there are free objects we can reuse..
        if (!freelist.empty()) {
            // ..use the last object that became unreachable (it will most likely be in cache).
            jint instance_number = freelist.back();
            assert(instance_number >= 1);
            freelist.pop_back(); // Remove it from the free list.
            assert(instances[instance_number - 1].isDead());
            instances[instance_number - 1] = std::move(instance);
            assert(instances[instance_number - 1].isLive());
            return instance_number;
        }

        // Otherwise, add a new one.
        instances.push_back(std::move(instance));
        return (jint) instances.size();
    }

    Tox4jStruct remove(jint instance_number) {
        return std::move(instances[instance_number - 1]);
    }

    void destroyAll() {
        for (Tox4jStruct &instance : instances) {
            Tox4jStruct dying(std::move(instance));
        }
    }

    bool empty() const { return instances.empty(); }
    size_t size() const { return instances.size(); }
};

static ToxInstances instances;


static std::string fullMessage(jint instance_number, char const *message) {
    std::ostringstream result;
    result << message << ", instance_number = " << instance_number;
    return result.str();
}

static inline void throw_tox_killed_exception(JNIEnv *env, jint instance_number, char const *message) {
    env->ThrowNew(env->FindClass("im/tox/tox4j/exceptions/ToxKilledException"),
        fullMessage(instance_number, message).c_str());
}

static inline void throw_illegal_state_exception(JNIEnv *env, jint instance_number, char const *message) {
    env->ThrowNew(env->FindClass("java/lang/IllegalStateException"),
        fullMessage(instance_number, message).c_str());
}
static inline void throw_illegal_state_exception(JNIEnv *env, jint instance_number, std::string const &message) {
    throw_illegal_state_exception(env, instance_number, message.c_str());
}

static inline void tox4j_assert(bool condition, JNIEnv *env, std::string const &message) {
    if (!condition) {
        env->FatalError(message.c_str());
    }
}

template<typename T> T default_value() { return T(); }
template<> void default_value<void>() { }

template<typename Func>
static typename std::result_of<Func(Tox *, ToxEvents &)>::type
with_instance(JNIEnv *env, jint instance_number, Func func)
{
    typedef typename std::result_of<Func(Tox *, ToxEvents &)>::type return_type;

    if (instance_number == 0) {
        throw_illegal_state_exception(env, instance_number, "Function called on incomplete object");
        return default_value<return_type>();
    }

    std::unique_lock<std::mutex> lock(instances.mutex);
    if (!instances.isValid(instance_number)) {
        throw_tox_killed_exception(env, instance_number, "Tox function invoked on invalid tox instance");
        return default_value<return_type>();
    }

    Tox4jStruct const &instance = instances[instance_number];

    if (!instance.isLive()) {
        throw_tox_killed_exception(env, instance_number, "Tox function invoked on killed tox instance");
        return default_value<return_type>();
    }

    std::lock_guard<std::mutex> ilock(*instance.mutex);
    Tox *tox = instance.tox.get();
    ToxEvents &events = *instance.events;
    lock.unlock();
    return func(tox, events);
}

/*
 * Do setup here. Caching of needed java method IDs etc should be done in this function. It is guaranteed to be called
 * when the library is loaded, and nothing else will be called before this function is called.
 */
jint JNI_OnLoad(JavaVM *, void *) {
    return JNI_VERSION_1_4;
}

JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_destroyAll(JNIEnv *, jclass) {
    std::unique_lock<std::mutex> lock(instances.mutex);
    instances.destroyAll();
}

JNIEXPORT jint JNICALL Java_im_tox_tox4j_Tox4j_toxNew(JNIEnv *env, jclass, jboolean ipv6enabled, jboolean udpDisabled,
    jboolean proxyEnabled, jstring proxyAddress, jint proxyPort) {
    auto opts = Tox_Options();
    opts.ipv6enabled = (uint8_t) ipv6enabled;
    opts.udp_disabled = (uint8_t) udpDisabled;
    if (proxyEnabled) {
        tox4j_assert(proxyAddress != NULL, env, "Proxy Address cannot be null when proxy is enabled");
        opts.proxy_enabled = true;
        strncpy(opts.proxy_address, UTFChars(env, proxyAddress), sizeof(opts.proxy_address) - 1);
        opts.proxy_port = (uint16_t) proxyPort;
    }

    std::unique_ptr<Tox, ToxDeleter> tox(tox_new(&opts));
    if (tox == nullptr) {
        return -1;
    }

    // We can create the new instance outside instances' critical section.
    Tox4jStruct instance {
        std::move(tox),
        std::unique_ptr<ToxEvents>(new ToxEvents),
        std::unique_ptr<std::mutex>(new std::mutex)
    };

    // This lock guards the instance manager.
    std::lock_guard<std::mutex> lock(instances.mutex);
    return instances.add(std::move(instance));
}

JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_kill(JNIEnv *env, jclass, jint instance_number) {
    std::lock_guard<std::mutex> lock(instances.mutex);

    if (instance_number < 0) {
        throw_illegal_state_exception(env, instance_number, "Tox instance out of range");
        return;
    }

    if (!instances.isValid(instance_number)) {
        throw_tox_killed_exception(env, instance_number, "close called on invalid instance");
        return;
    }

    // After this move, the pointers in instance_vector[instance_number] will all be nullptr...
    Tox4jStruct dying(instances.remove(instance_number));

    // ... so that this check will fail, if the function is called twice on the same instance.
    if (!dying.tox) {
        throw_tox_killed_exception(env, instance_number, "close called on already closed instance");
        return;
    }

    std::lock_guard<std::mutex> ilock(*dying.mutex);
}

JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_finalize(JNIEnv *env, jclass, jint instance_number) {
    if (instance_number == 0) {
        // This can happen when an exception is thrown from the constructor, giving this object an invalid state,
        // containing instance_number = 0.
        return;
    }

    if (instances.empty()) {
        throw_illegal_state_exception(env, instance_number, "Tox instance manager is empty");
        return;
    }

    std::lock_guard<std::mutex> lock(instances.mutex);
    if (!instances.isValid(instance_number)) {
        throw_illegal_state_exception(env, instance_number,
            "Tox instance out of range (max: " + std::to_string(instances.size() - 1) + ")");
        return;
    }

    // An instance should never be on this list twice.
    if (instances.isFree(instance_number)) {
        throw_illegal_state_exception(env, instance_number, "Tox instance already on free list");
        return;
    }

    instances.setFree(instance_number);
}

JNIEXPORT jint JNICALL Java_im_tox_tox4j_Tox4j_bootstrap(JNIEnv *env, jclass, jint instance_number,
    jstring address, jint port, jbyteArray public_key) {
    tox4j_assert(address != NULL, env, "Bootstrap address cannot be null");
    tox4j_assert(public_key != NULL, env, "Public key cannot be null");
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &) {
        return tox_bootstrap_from_address(tox, UTFChars(env, address), (uint16_t) port, ByteArray(env, public_key));
    });
}

JNIEXPORT jint JNICALL Java_im_tox_tox4j_Tox4j_addTcpRelay(JNIEnv *env, jclass, jint instance_number, jstring address,
    jint port, jbyteArray public_key) {
    tox4j_assert(address != NULL, env, "Relay address cannot be null");
    tox4j_assert(public_key != NULL, env, "Public key cannot be null");
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &) {
        return tox_add_tcp_relay(tox, UTFChars(env, address), (uint16_t) port, ByteArray(env, public_key));
    });
}

JNIEXPORT jboolean JNICALL Java_im_tox_tox4j_Tox4j_isConnected(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &) { return tox_isconnected(tox); });
}

JNIEXPORT jint JNICALL Java_im_tox_tox4j_Tox4j_doInterval(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &) { return tox_do_interval(tox); });
}

JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_Tox4j_toxDo(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        tox_do(tox);

        std::vector<char> buffer(events.ByteSize());
        events.SerializeToArray(buffer.data(), buffer.size());
        events.Clear();

        return toByteArray(env, buffer);
    });
}

JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_Tox4j_getAddress(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &) {
        std::vector<uint8_t> address(TOX_FRIEND_ADDRESS_SIZE);
        tox_get_address(tox, address.data());

        return toByteArray(env, address);
    });
}
