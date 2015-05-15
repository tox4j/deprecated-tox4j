#pragma once

#include "ToxInstances.h"
#include "jniutil.h"
#include "util/pp_cat.h"


#define METHOD(TYPE, NAME, ...) \
extern "C" JNIEXPORT TYPE JNICALL PP_CAT(Java_im_tox_tox4j_impl_, PP_CAT(CLASS, PP_CAT(Jni_, NAME))) \
  (JNIEnv *env, jclass, __VA_ARGS__)

#define TOX_METHOD(TYPE, NAME, ...) METHOD(TYPE, PP_CAT(PREFIX, NAME), __VA_ARGS__)
