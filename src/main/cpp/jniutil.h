#ifndef JNIUTIL_H
#define JNIUTIL_H

struct UTFChars {
    UTFChars(JNIEnv *env, jstring string)
    : env(env)
    , string(string)
    , chars(env->GetStringUTFChars(string, 0)) { }

    UTFChars(UTFChars const &) = delete;
    ~UTFChars() { env->ReleaseStringUTFChars(string, chars); }

    operator char const *() const { return chars; }

private:
    JNIEnv *env;
    jstring string;
    char const *chars;
};

struct ByteArray {
    ByteArray(JNIEnv *env, jbyteArray byteArray)
    : env(env)
    , byteArray(byteArray)
    , bytes(env->GetByteArrayElements(byteArray, 0)) { }

    ByteArray(ByteArray const &) = delete;
    ~ByteArray() { env->ReleaseByteArrayElements(byteArray, bytes, JNI_ABORT); }

    operator uint8_t const *() const { return (uint8_t *)bytes; }

private:
    JNIEnv *env;
    jbyteArray byteArray;
    jbyte *bytes;
};

#endif /* JNIUTIL_H */