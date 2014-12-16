#include <tox/tox.h>
#include "Tox4j.h"

ToxInstances ToxInstances::self;

/*
 * Do setup here. Caching of needed java method IDs etc should be done in this function. It is guaranteed to be called
 * when the library is loaded, and nothing else will be called before this function is called.
 */
jint JNI_OnLoad(JavaVM *, void *) {
    return JNI_VERSION_1_4;
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