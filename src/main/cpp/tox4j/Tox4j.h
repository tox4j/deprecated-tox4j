#pragma once

#include "ErrorHandling.h"
#include "ToxInstances.h"

#include <algorithm>
#include <sstream>
#include <stdexcept>
#include <utility>
#include <vector>


#define CAT(a, b) CAT_(a, b)
#define CAT_(a, b) a##b


#define METHOD(TYPE, NAME, ...) \
extern "C" JNIEXPORT TYPE JNICALL CAT(Java_im_tox_tox4j_, CAT(CLASS, CAT(Impl_, NAME))) \
  (JNIEnv *env, jclass, __VA_ARGS__)

#define TOX_METHOD(TYPE, NAME, ...) METHOD(TYPE, CAT(PREFIX, NAME), __VA_ARGS__)


template<typename T> static inline void unused (T const &) { }
