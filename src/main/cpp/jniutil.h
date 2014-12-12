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

    operator uint8_t const *() const { return (uint8_t *) bytes; }

    uint32_t length() {
        return (uint32_t) env->GetArrayLength(byteArray);
    }
private:
    JNIEnv *env;
    jbyteArray byteArray;
    jbyte *bytes;
};

template<typename T>
jbyteArray toByteArray(JNIEnv *env, std::vector<T> const &data) {
    static_assert(sizeof(T) == sizeof(jbyte), "Size requirements for byte array not met");
    jbyteArray jb = env->NewByteArray(data.size());
    env->SetByteArrayRegion(jb, 0, data.size(), (jbyte *) data.data());
    return jb;
}

#endif /* JNIUTIL_H */