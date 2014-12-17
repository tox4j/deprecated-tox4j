#include "ErrorHandling.h"


static std::string
fullMessage(jint instance_number, char const *message)
{
    std::ostringstream result;
    result << message << ", instance_number = " << instance_number;
    return result.str();
}

static void
throw_exception(JNIEnv *env, jint instance_number, char const *class_name, char const *message)
{
    env->ThrowNew(env->FindClass(class_name), fullMessage(instance_number, message).c_str());
}


void
throw_tox_killed_exception(JNIEnv *env, jint instance_number, char const *message)
{
    throw_exception(env, instance_number, "im/tox/tox4j/exceptions/ToxKilledException", message);
}

void
throw_illegal_state_exception(JNIEnv *env, jint instance_number, char const *message)
{
    throw_exception(env, instance_number, "java/lang/IllegalStateException", message);
}

void
throw_illegal_state_exception(JNIEnv *env, jint instance_number, std::string const &message)
{
    throw_exception(env, instance_number, "java/lang/IllegalStateException", message.c_str());
}


void
throw_tox_exception(JNIEnv *env, char const *method, char const *code)
{
    std::string className = "im/tox/tox4j/v2/exceptions/Tox";
    className += method;
    className += "Exception";
    jclass exClass = env->FindClass(className.c_str());
    assert(exClass);

    std::string enumName = className + "$Code";
    jclass enumClass = env->FindClass(enumName.c_str());
    assert(enumClass);

    std::string valueOfSig = "(Ljava/lang/String;)L" + enumName + ";";
    jmethodID valueOf = env->GetStaticMethodID(enumClass, "valueOf", valueOfSig.c_str());
    assert(valueOf);

    jobject enumCode = env->CallStaticObjectMethod(enumClass, valueOf, env->NewStringUTF(code));
    assert(enumCode);

    std::string constructorName = "(L" + enumName + ";)V";
    jmethodID constructor = env->GetMethodID(exClass, "<init>", constructorName.c_str());
    assert(constructor);

    jobject exception = env->NewObject(exClass, constructor, enumCode);
    assert(exception);

    env->Throw((jthrowable) exception);
}