#include "tox4j/Tox4j.h"
#include "jniutil.h"

#include "Core.pb.h"

#include "im_tox_tox4j_ToxCoreImpl.h"

#include <tox/core.h>

#define SUBSYSTEM TOX
#define CLASS     ToxCore
#define PREFIX    tox

namespace core
{
  namespace proto = im::tox::tox4j::core::proto;

  using Events = proto::CoreEvents;

  extern ToxInstances<tox::core_ptr, Events> instances;
}




template<typename T, size_t get_size (Tox const *), void get_data (Tox const *, T *)>
typename java_array_t<T>::array_type
get_vector (JNIEnv *env, Tox const *tox)
{
  size_t size = get_size (tox);
  if (size == 0)
    return nullptr;
  std::vector<T> name (size);
  get_data (tox, name.data ());

  return toJavaArray (env, name);
}


template<typename T, size_t size, void get_data (Tox const *, T *)>
typename java_array_t<T>::array_type
get_array (JNIEnv *env, Tox const *tox)
{
  std::vector<T> name (size);
  get_data (tox, name.data ());

  return toJavaArray (env, name);
}
