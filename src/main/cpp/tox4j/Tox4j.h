#pragma once

#include "ErrorHandling.h"

#include <algorithm>
#include <sstream>
#include <stdexcept>
#include <utility>
#include <vector>


#if defined(__GNUC__)
#  define TOX4J_NORETURN __attribute__((__noreturn__))
#else
#  define TOX4J_NORETURN
#endif


static inline TOX4J_NORETURN void
tox4j_fatal (JNIEnv *env, char const *message)
{
  env->FatalError (message);
  abort ();
}

#define fatal(message) tox4j_fatal (env, message)

#ifdef assert
#undef assert
#endif


#define STR(token) STR_(token)
#define STR_(token) #token
#define assert(condition) do {                                                  \
  if (!(condition))                                                             \
    fatal (__FILE__ ":" STR (__LINE__) ": Assertion failed: " #condition);      \
} while (0)


#define CAT(a, b) CAT_(a, b)
#define CAT_(a, b) a##b


#define METHOD(TYPE, NAME, ...) \
extern "C" JNIEXPORT TYPE JNICALL CAT(Java_im_tox_tox4j_impl_, CAT(CLASS, CAT(Impl_, NAME))) \
  (JNIEnv *env, jclass, __VA_ARGS__)

#define TOX_METHOD(TYPE, NAME, ...) METHOD(TYPE, CAT(PREFIX, NAME), __VA_ARGS__)


template<typename T> static inline void unused (T const &) { }
