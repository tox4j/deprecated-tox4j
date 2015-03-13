#include "ErrorHandling.h"


void
cosmic_ray_error (char const *function)
{
  fprintf (stderr, "Cosmic rays hit the wrong bits in toxcore (detected in %s)", function);
  abort ();
}


static std::string
fullMessage (jint instance_number, char const *message)
{
  std::ostringstream result;

  result << message << ", instance_number = " << instance_number;
  return result.str ();
}


static void
throw_exception (JNIEnv *env, jint instance_number, char const *class_name, char const *message)
{
  env->ThrowNew (env->FindClass (class_name), fullMessage (instance_number, message).c_str ());
}

void
throw_tox_killed_exception (JNIEnv *env, jint instance_number, char const *message)
{
  throw_exception (env, instance_number, "im/tox/tox4j/exceptions/ToxKilledException", message);
}

void
throw_illegal_state_exception (JNIEnv *env, jint instance_number, char const *message)
{
  throw_exception (env, instance_number, "java/lang/IllegalStateException", message);
}

void
throw_illegal_state_exception (JNIEnv *env, jint instance_number, std::string const &message)
{
  throw_exception (env, instance_number, "java/lang/IllegalStateException", message.c_str ());
}


void
throw_tox_exception (JNIEnv *env, char const *module, char const *method, char const *code)
{
  std::string className = "im/tox/tox4j/";

  className += module;
  className += "/exceptions/Tox";
  className += method;
  className += "Exception";
  jclass exClass = env->FindClass (className.c_str ());
  if (!exClass)
    {
      throw_exception (env, 0, "java/lang/NoClassDefFoundError", className.c_str ());
      return;
    }

  std::string enumName = className + "$Code";
  jclass enumClass = env->FindClass (enumName.c_str ());
  if (!enumClass)
    {
      throw_exception (env, 0, "java/lang/NoClassDefFoundError", enumName.c_str ());
      return;
    }

  std::string valueOfSig = "(Ljava/lang/String;)L" + enumName + ";";
  jmethodID valueOf = env->GetStaticMethodID (enumClass, "valueOf", valueOfSig.c_str ());
  if (!valueOf)
    {
      throw_exception (env, 0, "java/lang/NoSuchMethodException", ("valueOf" + valueOfSig).c_str ());
      return;
    }

  std::string constructorSig = "(L" + enumName + ";)V";
  jmethodID constructor = env->GetMethodID (exClass, "<init>", constructorSig.c_str ());
  if (!constructor)
    {
      throw_exception (env, 0, "java/lang/NoSuchMethodException", ("<init>" + constructorSig).c_str ());
      return;
    }

  jobject enumCode = env->CallStaticObjectMethod (enumClass, valueOf, env->NewStringUTF (code));
  assert (enumCode);

  jobject exception = env->NewObject (exClass, constructor, enumCode);
  assert (exception);

  env->Throw ((jthrowable)exception);
}


#ifdef HAVE_TOXAV
char const *const tox_traits<ToxAV>::module = "av";
#endif
char const *const tox_traits<Tox  >::module = "core";
