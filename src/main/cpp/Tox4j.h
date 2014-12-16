#include <algorithm>
#include <vector>
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

    static ToxInstances self;
};


static inline std::string fullMessage(jint instance_number, char const *message) {
    std::ostringstream result;
    result << message << ", instance_number = " << instance_number;
    return result.str();
}

static inline void
throw_tox_killed_exception(JNIEnv *env, jint instance_number, char const *message) {
    env->ThrowNew(env->FindClass("im/tox/tox4j/exceptions/ToxKilledException"),
        fullMessage(instance_number, message).c_str());
}

static inline void
throw_unsupported_operation_exception(JNIEnv *env, jint instance_number, char const *message) {
    env->ThrowNew(env->FindClass("java/lang/UnsupportedOperationException"),
        fullMessage(instance_number, message).c_str());
}

static inline void
throw_illegal_state_exception(JNIEnv *env, jint instance_number, char const *message) {
    env->ThrowNew(env->FindClass("java/lang/IllegalStateException"),
        fullMessage(instance_number, message).c_str());
}
static inline void
throw_illegal_state_exception(JNIEnv *env, jint instance_number, std::string const &message) {
    throw_illegal_state_exception(env, instance_number, message.c_str());
}


static inline void tox4j_assert(bool condition, JNIEnv *env, char const *message) {
    if (!condition) {
        env->FatalError(message);
    }
}

#ifdef assert
#undef assert
#endif

#define STR(token) STR_(token)
#define STR_(token) #token
#define assert(condition) do {                                                                  \
    tox4j_assert(condition, env, __FILE__ ":" STR(__LINE__) ": Assertion failed: " #condition); \
} while (0)


template<typename T> static inline T default_value() { return T(); }
template<> inline void default_value<void>() { }

template<typename Func>
static typename std::result_of<Func(Tox *, ToxEvents &)>::type
with_instance(JNIEnv *env, jint instance_number, Func func)
{
    typedef typename std::result_of<Func(Tox *, ToxEvents &)>::type return_type;

    if (instance_number == 0) {
        throw_illegal_state_exception(env, instance_number, "Function called on incomplete object");
        return default_value<return_type>();
    }

    std::unique_lock<std::mutex> lock(ToxInstances::self.mutex);
    if (!ToxInstances::self.isValid(instance_number)) {
        throw_tox_killed_exception(env, instance_number, "Tox function invoked on invalid tox instance");
        return default_value<return_type>();
    }

    Tox4jStruct const &instance = ToxInstances::self[instance_number];

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
