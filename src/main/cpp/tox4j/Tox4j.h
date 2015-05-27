#pragma once

#include "ToxInstances.h"
#include "jniutil.h"
#include "util/pp_cat.h"


#define TOX_METHOD(TYPE, NAME, ...) \
extern "C" JNIEXPORT TYPE JNICALL PP_CAT(Java_im_tox_tox4j_impl_jni_, PP_CAT(CLASS, PP_CAT(Jni_, PP_CAT(PREFIX, NAME)))) \
  (JNIEnv *env, jclass, __VA_ARGS__)
