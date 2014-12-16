#ifndef JNIUTIL_H
#define JNIUTIL_H

struct UTFChars {
    UTFChars(JNIEnv *env, jstring string)
    : env(env)
    , string(string)
    , chars(string ? env->GetStringUTFChars(string, 0) : nullptr) { }

    UTFChars(UTFChars const &) = delete;
    ~UTFChars() { if (string) env->ReleaseStringUTFChars(string, chars); }

    char const *data() const { return chars; }
    size_t size() const { return (size_t) string ? env->GetStringUTFLength(string) : 0; }

    operator char const *() const { return data(); }
    operator std::vector<char>() const { return std::vector<char>(data(), data() + size()); }

private:
    JNIEnv *env;
    jstring string;
    char const *chars;
};

struct ByteArray {
    ByteArray(JNIEnv *env, jbyteArray byteArray)
    : env(env)
    , byteArray(byteArray)
    , bytes(byteArray ? env->GetByteArrayElements(byteArray, 0) : nullptr) { }

    ByteArray(ByteArray const &) = delete;
    ~ByteArray() { if (byteArray) env->ReleaseByteArrayElements(byteArray, bytes, JNI_ABORT); }

    uint8_t const *data() const { return (uint8_t *) bytes; }
    size_t size() const { return (size_t) byteArray ? env->GetArrayLength(byteArray) : 0; }

    operator uint8_t const *() const { return data(); }
    operator std::vector<uint8_t>() const { return std::vector<uint8_t>(data(), data() + size()); }

private:
    JNIEnv *env;
    jbyteArray byteArray;
    jbyte *bytes;
};


template<typename JType, typename JavaArray, JavaArray (JNIEnv::*New)(jsize size), void (JNIEnv::*Set)(JavaArray, jsize, jsize, JType const *)>
struct make_java_array
{
    typedef JType java_type;
    typedef JavaArray array_type;

    static JavaArray make(JNIEnv *env, jsize size, JType const *data) {
        JavaArray array = (env->*New)(size);
        (env->*Set)(array, 0, size, data);
        return array;
    }
};

template<size_t Size>
struct java_array;

template<> struct java_array<sizeof(jbyte )> { typedef make_java_array<jbyte , jbyteArray , &JNIEnv::NewByteArray , &JNIEnv::SetByteArrayRegion > type; };
template<> struct java_array<sizeof(jshort)> { typedef make_java_array<jshort, jshortArray, &JNIEnv::NewShortArray, &JNIEnv::SetShortArrayRegion> type; };
template<> struct java_array<sizeof(jint  )> { typedef make_java_array<jint  , jintArray  , &JNIEnv::NewIntArray  , &JNIEnv::SetIntArrayRegion  > type; };
template<> struct java_array<sizeof(jlong )> { typedef make_java_array<jlong , jlongArray , &JNIEnv::NewLongArray , &JNIEnv::SetLongArrayRegion > type; };


template<typename T>
typename java_array<sizeof(T)>::type::array_type
toJavaArray(JNIEnv *env, std::vector<T> const &data) {
    typedef typename java_array<sizeof(T)>::type::java_type java_type;
    static_assert(sizeof(T) == sizeof(java_type), "Size requirements for java array not met");
    return java_array<sizeof(T)>::type::make(env, data.size(), reinterpret_cast<java_type const *>(data.data()));
}

#endif /* JNIUTIL_H */