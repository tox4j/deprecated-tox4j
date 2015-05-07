#pragma once

#include <jni.h>

#include <string>


void throw_tox_killed_exception (JNIEnv *env, jint instance_number, char const *message);
void throw_illegal_state_exception (JNIEnv *env, jint instance_number, char const *message);
void throw_illegal_state_exception (JNIEnv *env, jint instance_number, std::string const &message);
void throw_tox_exception (JNIEnv *env, char const *module, char const *method, char const *code);


#if defined(__GNUC__)
#  define TOX4J_NORETURN __attribute__((__noreturn__))
#else
#  define TOX4J_NORETURN
#endif


static inline TOX4J_NORETURN void
tox4j_fatal (JNIEnv *env, char const *message)
{
  env->FatalError (message);
  abort ();
}

#define tox4j_fatal(message) tox4j_fatal (env, message)


#define STR(token) STR_(token)
#define STR_(token) #token
#define tox4j_assert(condition) do {                                            \
  if (!(condition))                                                             \
    tox4j_fatal (__FILE__ ":" STR (__LINE__) ": Assertion failed: " #condition);\
} while (0)
