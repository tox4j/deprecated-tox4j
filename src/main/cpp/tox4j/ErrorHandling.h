#pragma once

#include <jni.h>

#include <sstream>


void throw_tox_killed_exception(JNIEnv *env, jint instance_number, char const *message);
void throw_illegal_state_exception(JNIEnv *env, jint instance_number, char const *message);
void throw_illegal_state_exception(JNIEnv *env, jint instance_number, std::string const &message);
void throw_tox_exception(JNIEnv *env, char const *method, char const *code);

#include "ToxInstances.h"


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

#define CAT(A, B)  CAT_(A, B)
#define CAT_(A, B) A##B

#define success_case(METHOD)                        \
    case CAT(SUBSYSTEM, _ERR_##METHOD##_OK):        \
        return success()

#define failure_case(METHOD, ERROR)                 \
    case CAT(SUBSYSTEM, _ERR_##METHOD##_##ERROR):   \
        return failure(#ERROR)


template<typename T> static inline T default_value() { return T(); }
template<> inline void default_value<void>() { }


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


namespace core {
    using Events = im::tox::tox4j::proto::CoreEvents;

    struct Deleter {
        void operator()(Tox *tox) {
            tox_kill(tox);
        }
    };

    struct tox_traits {
        typedef Tox subsystem;
        typedef Events events;
        typedef Deleter deleter;
    };
#include "with_instance.h"
}

namespace av {
    using Events = im::tox::tox4j::proto::AvEvents;

    struct Deleter {
        void operator()(ToxAV *av) {
            toxav_kill(av);
        }
    };

    struct tox_traits {
        typedef ToxAV subsystem;
        typedef Events events;
        typedef Deleter deleter;
    };
#include "with_instance.h"
}
