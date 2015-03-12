#pragma once

#include "ErrorHandling.h"

#include <algorithm>
#include <sstream>
#include <stdexcept>
#include <utility>
#include <vector>


static inline void
tox4j_assert (bool condition, JNIEnv *env, char const *message)
{
  if (!condition)
    env->FatalError (message);
}

#ifdef assert
#undef assert
#endif


#define STR(token) STR_(token)
#define STR_(token) #token
#define assert(condition) do {                                                                  \
  tox4j_assert (condition, env, __FILE__ ":" STR (__LINE__) ": Assertion failed: " #condition); \
} while (0)


#define CAT(a, b) CAT_(a, b)
#define CAT_(a, b) a##b


#define METHOD(TYPE, NAME, ...) \
JNIEXPORT TYPE JNICALL CAT(Java_im_tox_tox4j_, CAT(CLASS, CAT(Impl_, NAME))) \
  (JNIEnv *env, jclass, __VA_ARGS__)

#define TOX_METHOD(TYPE, NAME, ...) METHOD(TYPE, CAT(PREFIX, NAME), __VA_ARGS__)


template<typename T> static inline void unused (T const &) { }
