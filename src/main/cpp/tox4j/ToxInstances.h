#pragma once

#include <tox/av.h>
#include <tox/core.h>

#include "JTox.h"

#include "Av.pb.h"
#include "Core.pb.h"

#include <algorithm>
#include <memory>
#include <mutex>


template<typename T>
std::string to_string(T const &v) {
    std::ostringstream out;
    out << v;
    return out.str();
}


template<typename ToxTraits>
class tox_instance {
    // Objects of this class can conceptually have three states:
    // - LIVE: A tox instance is alive and running.
    // - DEAD: The object is empty, all pointers are nullptr.
    // - COLLECTED: state is DEAD, and the object's instance_number is in instance_freelist.
    bool live = true;

    typedef typename ToxTraits::subsystem subsystem_type;
    typedef typename ToxTraits::events events_type;
    typedef typename ToxTraits::deleter deleter_type;

public:
    typedef std::unique_ptr<subsystem_type, deleter_type> pointer;

    pointer tox;
    std::unique_ptr<events_type> events;
    std::unique_ptr<std::mutex> mutex;

    bool isLive() const { return live; }
    bool isDead() const { return !live; }

    tox_instance(pointer &&tox,
                 std::unique_ptr<events_type> &&events,
                 std::unique_ptr<std::mutex> &&mutex)
    : tox(std::move(tox))
    , events(std::move(events))
    , mutex(std::move(mutex))
    { }

    // Move members from another object into this new one, then set the old one to the DEAD state.
    tox_instance(tox_instance &&rhs)
    : tox(std::move(rhs.tox))
    , events(std::move(rhs.events))
    , mutex(std::move(rhs.mutex))
    { rhs.live = false; }

    // Move members from another object into this existing one, then set the right hand side to the DEAD state.
    // This object is then live again.
    tox_instance &operator=(tox_instance &&rhs) {
        assert(this->isDead());
        assert(rhs.isLive());

        tox = std::move(rhs.tox);
        events = std::move(rhs.events);
        mutex = std::move(rhs.mutex);
        rhs.live = false;
        this->live = true;

        return *this;
    }
};

template<typename ToxTraits>
class instance_manager {
    typedef tox_instance<ToxTraits> instance_type;

    // This struct should remain small. Check some assumptions here.
    static_assert(sizeof(instance_type) == sizeof(void *) * 4,
        "tox_instance has unexpected members or padding");

    std::vector<instance_type> instances;
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

    instance_type const &operator[](jint instance_number) const {
        return instances[instance_number - 1];
    }

    jint add(instance_type &&instance) {
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

private:
    instance_type remove(jint instance_number) {
        return std::move(instances[instance_number - 1]);
    }

    bool empty() const { return instances.empty(); }
    size_t size() const { return instances.size(); }


public:
    void destroyAll() {
        for (instance_type &instance : instances) {
            instance_type dying(std::move(instance));
        }
    }

    void kill(JNIEnv *env, jint instanceNumber)
    {
        std::lock_guard<std::mutex> lock(mutex);

        if (instanceNumber < 0) {
            throw_illegal_state_exception(env, instanceNumber, "Tox instance out of range");
            return;
        }

        if (!isValid(instanceNumber)) {
            throw_tox_killed_exception(env, instanceNumber, "close called on invalid instance");
            return;
        }

        // After this move, the pointers in instance_vector[instance_number] will all be nullptr...
        instance_type dying(remove(instanceNumber));

        // ... so that this check will fail, if the function is called twice on the same instance.
        if (!dying.tox) {
            throw_tox_killed_exception(env, instanceNumber, "close called on already closed instance");
            return;
        }

        assert(dying.isLive());
        std::lock_guard<std::mutex> ilock(*dying.mutex);
    }

    void finalize(JNIEnv *env, jint instanceNumber)
    {
        if (instanceNumber == 0) {
            // This can happen when an exception is thrown from the constructor, giving this object an invalid state,
            // containing instanceNumber = 0.
            return;
        }

        if (empty()) {
            throw_illegal_state_exception(env, instanceNumber, "Tox instance manager is empty");
            return;
        }

        std::lock_guard<std::mutex> lock(mutex);
        if (!isValid(instanceNumber)) {
            throw_illegal_state_exception(env, instanceNumber,
                "Tox instance out of range (max: " + to_string(size() - 1) + ")");
            return;
        }

        // An instance should never be on this list twice.
        if (isFree(instanceNumber)) {
            throw_illegal_state_exception(env, instanceNumber, "Tox instance already on free list");
            return;
        }

        // This instance was leaked, kill it before setting it free.
        if ((*this)[instanceNumber].isLive()) {
            instance_type dying(remove(instanceNumber));
            assert(dying.isLive());
            std::lock_guard<std::mutex> ilock(*dying.mutex);
        }

        assert((*this)[instanceNumber].isDead());
        setFree(instanceNumber);
    }

    static instance_manager self;
};

template<typename ToxTraits>
instance_manager<ToxTraits> instance_manager<ToxTraits>::self;
