#ifndef JNIUTIL_H
#define JNIUTIL_H

struct UTFChars {
    UTFChars(JNIEnv *env, jstring string)
    : env(env)
    , string(string)
    , chars(env->GetStringUTFChars(string, 0)) { }

    UTFChars(UTFChars const &) = delete;
    ~UTFChars() { env->ReleaseStringUTFChars(string, chars); }

    char const *data() const { return chars; }
    size_t length() const { return (size_t) env->GetStringUTFLength(string); }

    operator char const *() const { return data(); }
    operator std::vector<char>() const { return std::vector<char>(data(), data() + length()); }

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

    uint8_t const *data() const { return (uint8_t *) bytes; }
    size_t length() const { return (size_t) env->GetArrayLength(byteArray); }

    operator uint8_t const *() const { return data(); }
    operator std::vector<uint8_t>() const { return std::vector<uint8_t>(data(), data() + length()); }

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