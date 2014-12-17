#pragma once

#include <jni.h>

#include <sstream>

#include "ToxInstances.h"


template<typename T> static inline T default_value() { return T(); }
template<> inline void default_value<void>() { }


void throw_unsupported_operation_exception(JNIEnv *env, jint instance_number, char const *message);
void throw_tox_killed_exception(JNIEnv *env, jint instance_number, char const *message);
void throw_illegal_state_exception(JNIEnv *env, jint instance_number, char const *message);
void throw_illegal_state_exception(JNIEnv *env, jint instance_number, std::string const &message);
void throw_tox_exception(JNIEnv *env, char const *method, char const *code);


template<typename Func>
typename std::result_of<Func(Tox *, ToxEvents &)>::type
with_instance(JNIEnv *env, jint instance_number, Func func)
{
    typedef typename std::result_of<Func(Tox *, ToxEvents &)>::type return_type;

    if (instance_number == 0) {
        throw_illegal_state_exception(env, instance_number, "Function called on incomplete object");
        return default_value<return_type>();
    }

    std::unique_lock<std::mutex> lock(InstanceManager::self.mutex);
    if (!InstanceManager::self.isValid(instance_number)) {
        throw_tox_killed_exception(env, instance_number, "Tox function invoked on invalid tox instance");
        return default_value<return_type>();
    }

    ToxInstance const &instance = InstanceManager::self[instance_number];

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


struct ErrorHandling
{
    enum Result
    {
        UNHANDLED,
        SUCCESS,
        FAILURE
    };

    Result const result;
    char const *const error;
};

static inline ErrorHandling
success()
{
    return ErrorHandling { ErrorHandling::SUCCESS, nullptr };
}

static inline ErrorHandling
failure(char const *error)
{
    return ErrorHandling { ErrorHandling::FAILURE, error };
}

static inline ErrorHandling
unhandled()
{
    return ErrorHandling { ErrorHandling::UNHANDLED, nullptr };
}


template<typename FuncT>
struct error_type_of;

template<typename Result, typename Head, typename... Tail>
struct error_type_of<Result(*)(Head, Tail...)>
{
    typedef typename error_type_of<Result(*)(Tail...)>::type type;
};

template<typename Result, typename ErrorCode>
struct error_type_of<Result(*)(ErrorCode *)>
{
    typedef ErrorCode type;
};

template<typename FuncT>
using tox_error_t = typename error_type_of<FuncT>::type;

template<typename SuccessFunc, typename ToxFunc, typename... Args>
using tox_success_t = typename std::result_of<SuccessFunc(typename std::result_of<ToxFunc(Args..., tox_error_t<ToxFunc> *)>::type)>::type;


template<typename ErrorFunc, typename SuccessFunc, typename ToxFunc, typename... Args>
tox_success_t<SuccessFunc, ToxFunc, Args...>
with_error_handling(JNIEnv *env, char const *method, ErrorFunc error_func, SuccessFunc success_func, ToxFunc tox_func, Args ...args)
{
    tox_error_t<ToxFunc> error;
    auto value = tox_func(args..., &error);
    ErrorHandling result = error_func(error);
    switch (result.result) {
        case ErrorHandling::SUCCESS:
            return success_func(value);
        case ErrorHandling::FAILURE:
            throw_tox_exception(env, method, result.error);
            break;
        case ErrorHandling::UNHANDLED:
            throw_illegal_state_exception(env, error, "Unknown error code");
            break;
    }

    return default_value<tox_success_t<SuccessFunc, ToxFunc, Args...>>();
}


template<typename ErrorFunc, typename SuccessFunc, typename ToxFunc, typename... Args>
tox_success_t<SuccessFunc, ToxFunc, Tox *, Args...>
with_instance(JNIEnv *env, jint instanceNumber, char const *method, ErrorFunc error_func, SuccessFunc success_func, ToxFunc tox_func, Args ...args)
{
    return with_instance(env, instanceNumber, [=](Tox *tox, ToxEvents &events) {
        (void)events;
        return with_error_handling(env, method, error_func, success_func, tox_func, tox, args...);
    });
}
