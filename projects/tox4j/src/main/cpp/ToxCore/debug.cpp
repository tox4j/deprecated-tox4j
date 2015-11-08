#include "ToxCore.h"

#ifdef TOX_VERSION_MAJOR

#include <algorithm>
#include <vector>

template<>
void
print_arg<Tox *> (protolog::Value &value, Tox *tox)
{
  static std::vector<Tox *> ids;
  auto found = std::find (ids.begin (), ids.end (), tox);
  if (found == ids.end ())
    {
      ids.push_back (tox);
      found = ids.end () - 1;
    }
  value.set_string ("@" + std::to_string (found - ids.begin () + 1));
}

template<>
void
print_arg<Tox_Options *> (protolog::Value &value, Tox_Options *options)
{
  if (options == nullptr)
    value.set_string ("<null>");
  else
    {
      print_member (value, "ipv6_enabled", options->ipv6_enabled);
      print_member (value, "udp_enabled", options->udp_enabled);
      print_member (value, "proxy_type", options->proxy_type);
      print_member (value, "proxy_host", options->proxy_host);
      print_member (value, "start_port", options->start_port);
      print_member (value, "end_port", options->end_port);
      print_member (value, "tcp_port", options->tcp_port);
      print_member (value, "savedata_type", options->savedata_type);
      print_member (value, "savedata", options->savedata_data, options->savedata_length);
    }
}

template<>
void
print_arg<core::Events *> (protolog::Value &value, core::Events *events)
{
  if (events != nullptr)
    value.set_string ("<core::Events[" + std::to_string (events->ByteSize ()) + "]>");
  else
    value.set_string ("<null>");
}

#define enum_case(ENUM)                               \
    case TOX_##ENUM: value.set_string ("TOX_" #ENUM); break

template<>
void
print_arg<TOX_FILE_KIND> (protolog::Value &value, TOX_FILE_KIND kind)
{
  switch (kind)
    {
    enum_case (FILE_KIND_DATA);
    enum_case (FILE_KIND_AVATAR);
    default:
      value.set_string ("(TOX_FILE_KIND)" + std::to_string (kind));
      break;
    }
}

/*
 * Class:     im_tox_tox4j_impl_jni_ToxCoreJni
 * Method:    tox4jLastLog
 * Signature: ()[B
 */
JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_impl_jni_ToxCoreJni_tox4jLastLog
  (JNIEnv *env, jclass)
{
  if (jni_log.empty ())
    return nullptr;

  return toJavaArray (env, jni_log.clear ());
}

/*
 * Class:     im_tox_tox4j_impl_jni_ToxCoreJni
 * Method:    tox4jSetLogging
 * Signature: (Z)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_impl_jni_ToxCoreJni_tox4jSetLogging
  (JNIEnv *, jclass, jboolean enabled)
{
  jni_log.enabled (enabled);
}

/*
 * Class:     im_tox_tox4j_impl_jni_ToxCoreJni
 * Method:    tox4jSetMaxLogSize
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_impl_jni_ToxCoreJni_tox4jSetMaxLogSize
  (JNIEnv *, jclass, jint maxSize)
{
  jni_log.max_size (maxSize);
}

#endif
