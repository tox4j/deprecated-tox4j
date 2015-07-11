#include "../ToxCore.h"

#ifdef TOX_VERSION_MAJOR


template<>
jint
enum_ordinal<TOX_CONNECTION> (JNIEnv *env, TOX_CONNECTION value)
{
  switch (value)
    {
    case TOX_CONNECTION_NONE: return 0;
    case TOX_CONNECTION_TCP: return 1;
    case TOX_CONNECTION_UDP: return 2;
    }
  tox4j_fatal ("Invalid enumerator from toxcore");
}

template<>
TOX_CONNECTION
enum_value<TOX_CONNECTION> (JNIEnv *env, jint ordinal)
{
  switch (ordinal)
    {
    case 0: return TOX_CONNECTION_NONE;
    case 1: return TOX_CONNECTION_TCP;
    case 2: return TOX_CONNECTION_UDP;
    }
  tox4j_fatal ("Invalid enumerator from Java");
}

template<>
void
print_arg<TOX_CONNECTION> (TOX_CONNECTION value)
{
  switch (value)
    {
    case TOX_CONNECTION_NONE: debug_out << "TOX_CONNECTION_NONE"; break;
    case TOX_CONNECTION_TCP: debug_out << "TOX_CONNECTION_TCP"; break;
    case TOX_CONNECTION_UDP: debug_out << "TOX_CONNECTION_UDP"; break;
    default: debug_out << "(TOX_CONNECTION)" << value; break;
    }
}


template<>
jint
enum_ordinal<TOX_FILE_CONTROL> (JNIEnv *env, TOX_FILE_CONTROL value)
{
  switch (value)
    {
    case TOX_FILE_CONTROL_RESUME: return 0;
    case TOX_FILE_CONTROL_PAUSE: return 1;
    case TOX_FILE_CONTROL_CANCEL: return 2;
    }
  tox4j_fatal ("Invalid enumerator from toxcore");
}

template<>
TOX_FILE_CONTROL
enum_value<TOX_FILE_CONTROL> (JNIEnv *env, jint ordinal)
{
  switch (ordinal)
    {
    case 0: return TOX_FILE_CONTROL_RESUME;
    case 1: return TOX_FILE_CONTROL_PAUSE;
    case 2: return TOX_FILE_CONTROL_CANCEL;
    }
  tox4j_fatal ("Invalid enumerator from Java");
}

template<>
void
print_arg<TOX_FILE_CONTROL> (TOX_FILE_CONTROL value)
{
  switch (value)
    {
    case TOX_FILE_CONTROL_RESUME: debug_out << "TOX_FILE_CONTROL_RESUME"; break;
    case TOX_FILE_CONTROL_PAUSE: debug_out << "TOX_FILE_CONTROL_PAUSE"; break;
    case TOX_FILE_CONTROL_CANCEL: debug_out << "TOX_FILE_CONTROL_CANCEL"; break;
    default: debug_out << "(TOX_FILE_CONTROL)" << value; break;
    }
}


template<>
jint
enum_ordinal<TOX_MESSAGE_TYPE> (JNIEnv *env, TOX_MESSAGE_TYPE value)
{
  switch (value)
    {
    case TOX_MESSAGE_TYPE_NORMAL: return 0;
    case TOX_MESSAGE_TYPE_ACTION: return 1;
    }
  tox4j_fatal ("Invalid enumerator from toxcore");
}

template<>
TOX_MESSAGE_TYPE
enum_value<TOX_MESSAGE_TYPE> (JNIEnv *env, jint ordinal)
{
  switch (ordinal)
    {
    case 0: return TOX_MESSAGE_TYPE_NORMAL;
    case 1: return TOX_MESSAGE_TYPE_ACTION;
    }
  tox4j_fatal ("Invalid enumerator from Java");
}

template<>
void
print_arg<TOX_MESSAGE_TYPE> (TOX_MESSAGE_TYPE value)
{
  switch (value)
    {
    case TOX_MESSAGE_TYPE_NORMAL: debug_out << "TOX_MESSAGE_TYPE_NORMAL"; break;
    case TOX_MESSAGE_TYPE_ACTION: debug_out << "TOX_MESSAGE_TYPE_ACTION"; break;
    default: debug_out << "(TOX_MESSAGE_TYPE)" << value; break;
    }
}


template<>
jint
enum_ordinal<TOX_PROXY_TYPE> (JNIEnv *env, TOX_PROXY_TYPE value)
{
  switch (value)
    {
    case TOX_PROXY_TYPE_NONE: return 0;
    case TOX_PROXY_TYPE_HTTP: return 1;
    case TOX_PROXY_TYPE_SOCKS5: return 2;
    }
  tox4j_fatal ("Invalid enumerator from toxcore");
}

template<>
TOX_PROXY_TYPE
enum_value<TOX_PROXY_TYPE> (JNIEnv *env, jint ordinal)
{
  switch (ordinal)
    {
    case 0: return TOX_PROXY_TYPE_NONE;
    case 1: return TOX_PROXY_TYPE_HTTP;
    case 2: return TOX_PROXY_TYPE_SOCKS5;
    }
  tox4j_fatal ("Invalid enumerator from Java");
}

template<>
void
print_arg<TOX_PROXY_TYPE> (TOX_PROXY_TYPE value)
{
  switch (value)
    {
    case TOX_PROXY_TYPE_NONE: debug_out << "TOX_PROXY_TYPE_NONE"; break;
    case TOX_PROXY_TYPE_HTTP: debug_out << "TOX_PROXY_TYPE_HTTP"; break;
    case TOX_PROXY_TYPE_SOCKS5: debug_out << "TOX_PROXY_TYPE_SOCKS5"; break;
    default: debug_out << "(TOX_PROXY_TYPE)" << value; break;
    }
}


template<>
jint
enum_ordinal<TOX_SAVEDATA_TYPE> (JNIEnv *env, TOX_SAVEDATA_TYPE value)
{
  switch (value)
    {
    case TOX_SAVEDATA_TYPE_NONE: return 0;
    case TOX_SAVEDATA_TYPE_TOX_SAVE: return 1;
    case TOX_SAVEDATA_TYPE_SECRET_KEY: return 2;
    }
  tox4j_fatal ("Invalid enumerator from toxcore");
}

template<>
TOX_SAVEDATA_TYPE
enum_value<TOX_SAVEDATA_TYPE> (JNIEnv *env, jint ordinal)
{
  switch (ordinal)
    {
    case 0: return TOX_SAVEDATA_TYPE_NONE;
    case 1: return TOX_SAVEDATA_TYPE_TOX_SAVE;
    case 2: return TOX_SAVEDATA_TYPE_SECRET_KEY;
    }
  tox4j_fatal ("Invalid enumerator from Java");
}

template<>
void
print_arg<TOX_SAVEDATA_TYPE> (TOX_SAVEDATA_TYPE value)
{
  switch (value)
    {
    case TOX_SAVEDATA_TYPE_NONE: debug_out << "TOX_SAVEDATA_TYPE_NONE"; break;
    case TOX_SAVEDATA_TYPE_TOX_SAVE: debug_out << "TOX_SAVEDATA_TYPE_TOX_SAVE"; break;
    case TOX_SAVEDATA_TYPE_SECRET_KEY: debug_out << "TOX_SAVEDATA_TYPE_SECRET_KEY"; break;
    default: debug_out << "(TOX_SAVEDATA_TYPE)" << value; break;
    }
}


template<>
jint
enum_ordinal<TOX_USER_STATUS> (JNIEnv *env, TOX_USER_STATUS value)
{
  switch (value)
    {
    case TOX_USER_STATUS_NONE: return 0;
    case TOX_USER_STATUS_AWAY: return 1;
    case TOX_USER_STATUS_BUSY: return 2;
    }
  tox4j_fatal ("Invalid enumerator from toxcore");
}

template<>
TOX_USER_STATUS
enum_value<TOX_USER_STATUS> (JNIEnv *env, jint ordinal)
{
  switch (ordinal)
    {
    case 0: return TOX_USER_STATUS_NONE;
    case 1: return TOX_USER_STATUS_AWAY;
    case 2: return TOX_USER_STATUS_BUSY;
    }
  tox4j_fatal ("Invalid enumerator from Java");
}

template<>
void
print_arg<TOX_USER_STATUS> (TOX_USER_STATUS value)
{
  switch (value)
    {
    case TOX_USER_STATUS_NONE: debug_out << "TOX_USER_STATUS_NONE"; break;
    case TOX_USER_STATUS_AWAY: debug_out << "TOX_USER_STATUS_AWAY"; break;
    case TOX_USER_STATUS_BUSY: debug_out << "TOX_USER_STATUS_BUSY"; break;
    default: debug_out << "(TOX_USER_STATUS)" << value; break;
    }
}


#endif
