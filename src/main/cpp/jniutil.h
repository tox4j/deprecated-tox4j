#ifndef JNIUTIL_H
#define JNIUTIL_H

#include <type_traits>


/*****************************************************************************
 * UTF-8 encoded Java string as C++ char array.
 */

struct UTFChars
{
  UTFChars (JNIEnv *env, jstring string)
    : env (env)
    , string (string)
    , chars (string ? env->GetStringUTFChars (string, 0) : nullptr)
  { }

  UTFChars (UTFChars const &) = delete;
  ~UTFChars () { if (string) env->ReleaseStringUTFChars (string, chars); }

  char const *data () const { return chars; }

  size_t size () const { return (size_t)string ? env->GetStringUTFLength (string) : 0; }

private:
  JNIEnv *env;
  jstring string;
  char const *chars;
};


/*****************************************************************************
 * Java arrays as C++ arrays.
 */

template<
  typename JType,
  typename CType,
  typename JavaArray,
  JType *(JNIEnv::*GetArrayElements) (JavaArray, jboolean *),
  void (JNIEnv::*ReleaseArrayElements) (JavaArray, JType *, jint)
>
struct make_c_array
{
  static_assert (sizeof (JType) == sizeof (CType),
                 "Size requirements for Java array not met");

  make_c_array (JNIEnv *env, JavaArray jArray)
    : env (env)
    , jArray (jArray)
    , cArray (jArray ? (env->*GetArrayElements) (jArray, nullptr) : nullptr)
  {
  }

  make_c_array (make_c_array const &) = delete;
  ~make_c_array () { if (jArray) (env->*ReleaseArrayElements) (jArray, cArray, JNI_ABORT); }

  CType const *data () const { return reinterpret_cast<CType const *> (cArray); }

  size_t size () const { return jArray ? env->GetArrayLength (jArray) : 0; }
  bool empty () const { return size () == 0; }

private:
  JNIEnv *env;
  JavaArray jArray;
  JType *cArray;
};

using BooleanArray = make_c_array<jboolean, bool    , jbooleanArray, &JNIEnv::GetBooleanArrayElements, &JNIEnv::ReleaseBooleanArrayElements>;
using ByteArray    = make_c_array<jbyte   , uint8_t , jbyteArray   , &JNIEnv::GetByteArrayElements   , &JNIEnv::ReleaseByteArrayElements   >;
using CharArray    = make_c_array<jchar   , uint16_t, jcharArray   , &JNIEnv::GetCharArrayElements   , &JNIEnv::ReleaseCharArrayElements   >;
using ShortArray   = make_c_array<jshort  , int16_t , jshortArray  , &JNIEnv::GetShortArrayElements  , &JNIEnv::ReleaseShortArrayElements  >;
using IntArray     = make_c_array<jint    , int32_t , jintArray    , &JNIEnv::GetIntArrayElements    , &JNIEnv::ReleaseIntArrayElements    >;
using LongArray    = make_c_array<jlong   , int64_t , jlongArray   , &JNIEnv::GetLongArrayElements   , &JNIEnv::ReleaseLongArrayElements   >;
using FloatArray   = make_c_array<jfloat  , float   , jfloatArray  , &JNIEnv::GetFloatArrayElements  , &JNIEnv::ReleaseFloatArrayElements  >;
using DoubleArray  = make_c_array<jdouble , double  , jdoubleArray , &JNIEnv::GetDoubleArrayElements , &JNIEnv::ReleaseDoubleArrayElements >;


/*****************************************************************************
 * C++ arrays as Java arrays.
 */

template<
  typename JType,
  typename JavaArray,
  JavaArray (JNIEnv::*New) (jsize size),
  void (JNIEnv::*Set) (JavaArray, jsize, jsize, JType const *)
>
struct make_java_array
{
  typedef JType java_type;
  typedef JavaArray array_type;

  static JavaArray make (JNIEnv *env, jsize size, JType const *data)
  {
    JavaArray array = (env->*New)(size);

    (env->*Set) (array, 0, size, data);
    return array;
  }
};

template<typename JType>
struct java_array;

template<> struct java_array<jboolean> { typedef make_java_array<jboolean, jbooleanArray, &JNIEnv::NewBooleanArray, &JNIEnv::SetBooleanArrayRegion> type; };
template<> struct java_array<jbyte   > { typedef make_java_array<jbyte   , jbyteArray   , &JNIEnv::NewByteArray   , &JNIEnv::SetByteArrayRegion   > type; };
template<> struct java_array<jchar   > { typedef make_java_array<jchar   , jcharArray   , &JNIEnv::NewCharArray   , &JNIEnv::SetCharArrayRegion   > type; };
template<> struct java_array<jshort  > { typedef make_java_array<jshort  , jshortArray  , &JNIEnv::NewShortArray  , &JNIEnv::SetShortArrayRegion  > type; };
template<> struct java_array<jint    > { typedef make_java_array<jint    , jintArray    , &JNIEnv::NewIntArray    , &JNIEnv::SetIntArrayRegion    > type; };
template<> struct java_array<jlong   > { typedef make_java_array<jlong   , jlongArray   , &JNIEnv::NewLongArray   , &JNIEnv::SetLongArrayRegion   > type; };
template<> struct java_array<jfloat  > { typedef make_java_array<jfloat  , jfloatArray  , &JNIEnv::NewFloatArray  , &JNIEnv::SetFloatArrayRegion  > type; };
template<> struct java_array<jdouble > { typedef make_java_array<jdouble , jdoubleArray , &JNIEnv::NewDoubleArray , &JNIEnv::SetDoubleArrayRegion > type; };

template<typename CType>
using java_array_t = typename java_array<typename std::make_signed<CType>::type>::type;


template<typename T>
typename java_array_t<T>::array_type
toJavaArray (JNIEnv *env, std::vector<T> const &data)
{
  typedef typename java_array_t<T>::java_type java_type;
  static_assert (sizeof (T) == sizeof (java_type),
                 "Size requirements for Java array not met");
  return java_array_t<T>::make (env, data.size (),
                                reinterpret_cast<java_type const *> (data.data ()));
}



template<typename T, size_t get_size (Tox const *), void get_data (Tox const *, T *)>
typename java_array_t<T>::array_type
get_vector (JNIEnv *env, Tox *tox)
{
  size_t size = get_size (tox);
  if (size == 0)
    return nullptr;
  std::vector<T> name (size);
  get_data (tox, name.data ());

  return toJavaArray (env, name);
}

#define GET_VECTOR(T, FROM, WHAT)		\
  [env] (Tox *tox, Events &)			\
    {						\
      return get_vector<T,			\
        tox_##FROM##_get_##WHAT##_size,		\
        tox_##FROM##_get_##WHAT> (env, tox);	\
    }


template<typename T, size_t size, void get_data (Tox const *, T *)>
typename java_array_t<T>::array_type
get_array (JNIEnv *env, Tox *tox)
{
  std::vector<T> name (size);
  get_data (tox, name.data ());

  return toJavaArray (env, name);
}

#define GET_ARRAY(T, FROM, WHAT, SIZE)		\
  [env] (Tox *tox, Events &)			\
    {						\
      return get_array<T,			\
        SIZE,					\
        tox_##FROM##_get_##WHAT> (env, tox);	\
    }


#endif /* JNIUTIL_H */
