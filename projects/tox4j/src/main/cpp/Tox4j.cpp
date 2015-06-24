#include <jni.h>

#include <tox/tox.h>


/*
 * Do setup here. Caching of needed java method IDs etc should be done in this
 * function. It is guaranteed to be called when the library is loaded, and
 * nothing else will be called before this function is called.
 */
jint
JNI_OnLoad (JavaVM *, void *)
{
  if (!TOX_VERSION_IS_ABI_COMPATIBLE ())
    return -1;
  return JNI_VERSION_1_4;
}
