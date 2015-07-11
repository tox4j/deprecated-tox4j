#include "../ToxAv.h"

#ifdef TOXAV_VERSION_MAJOR

template<>
jint
enum_ordinal<TOXAV_CALL_CONTROL> (JNIEnv *env, TOXAV_CALL_CONTROL value)
{
  switch (value)
    {
    case TOXAV_CALL_CONTROL_RESUME: return 0;
    case TOXAV_CALL_CONTROL_PAUSE: return 1;
    case TOXAV_CALL_CONTROL_CANCEL: return 2;
    case TOXAV_CALL_CONTROL_MUTE_AUDIO: return 3;
    case TOXAV_CALL_CONTROL_UNMUTE_AUDIO: return 4;
    case TOXAV_CALL_CONTROL_HIDE_VIDEO: return 5;
    case TOXAV_CALL_CONTROL_SHOW_VIDEO: return 6;
    }
  tox4j_fatal ("Invalid enumerator from toxcore");
}

template<>
TOXAV_CALL_CONTROL
enum_value<TOXAV_CALL_CONTROL> (JNIEnv *env, jint ordinal)
{
  switch (ordinal)
    {
    case 0: return TOXAV_CALL_CONTROL_RESUME;
    case 1: return TOXAV_CALL_CONTROL_PAUSE;
    case 2: return TOXAV_CALL_CONTROL_CANCEL;
    case 3: return TOXAV_CALL_CONTROL_MUTE_AUDIO;
    case 4: return TOXAV_CALL_CONTROL_UNMUTE_AUDIO;
    case 5: return TOXAV_CALL_CONTROL_HIDE_VIDEO;
    case 6: return TOXAV_CALL_CONTROL_SHOW_VIDEO;
    }
  tox4j_fatal ("Invalid enumerator from Java");
}

template<>
void
print_arg<TOXAV_CALL_CONTROL> (TOXAV_CALL_CONTROL value)
{
  switch (value)
    {
    case TOXAV_CALL_CONTROL_RESUME: debug_out << "TOXAV_CALL_CONTROL_RESUME"; return;
    case TOXAV_CALL_CONTROL_PAUSE: debug_out << "TOXAV_CALL_CONTROL_PAUSE"; return;
    case TOXAV_CALL_CONTROL_CANCEL: debug_out << "TOXAV_CALL_CONTROL_CANCEL"; return;
    case TOXAV_CALL_CONTROL_MUTE_AUDIO: debug_out << "TOXAV_CALL_CONTROL_MUTE_AUDIO"; return;
    case TOXAV_CALL_CONTROL_UNMUTE_AUDIO: debug_out << "TOXAV_CALL_CONTROL_UNMUTE_AUDIO"; return;
    case TOXAV_CALL_CONTROL_HIDE_VIDEO: debug_out << "TOXAV_CALL_CONTROL_HIDE_VIDEO"; return;
    case TOXAV_CALL_CONTROL_SHOW_VIDEO: debug_out << "TOXAV_CALL_CONTROL_SHOW_VIDEO"; return;
    }
  debug_out << "(TOXAV_CALL_CONTROL)" << value;
}

template<>
jint
enum_ordinal<TOXAV_FRIEND_CALL_STATE> (JNIEnv *env, TOXAV_FRIEND_CALL_STATE value)
{
  switch (value)
    {
    case TOXAV_FRIEND_CALL_STATE_ERROR: return 0;
    case TOXAV_FRIEND_CALL_STATE_FINISHED: return 1;
    case TOXAV_FRIEND_CALL_STATE_SENDING_A: return 2;
    case TOXAV_FRIEND_CALL_STATE_SENDING_V: return 3;
    case TOXAV_FRIEND_CALL_STATE_ACCEPTING_A: return 4;
    case TOXAV_FRIEND_CALL_STATE_ACCEPTING_V: return 5;
    }
  tox4j_fatal ("Invalid enumerator from toxcore");
}

template<>
TOXAV_FRIEND_CALL_STATE
enum_value<TOXAV_FRIEND_CALL_STATE> (JNIEnv *env, jint ordinal)
{
  switch (ordinal)
    {
    case 0: return TOXAV_FRIEND_CALL_STATE_ERROR;
    case 1: return TOXAV_FRIEND_CALL_STATE_FINISHED;
    case 2: return TOXAV_FRIEND_CALL_STATE_SENDING_A;
    case 3: return TOXAV_FRIEND_CALL_STATE_SENDING_V;
    case 4: return TOXAV_FRIEND_CALL_STATE_ACCEPTING_A;
    case 5: return TOXAV_FRIEND_CALL_STATE_ACCEPTING_V;
    }
  tox4j_fatal ("Invalid enumerator from Java");
}

template<>
void
print_arg<TOXAV_FRIEND_CALL_STATE> (TOXAV_FRIEND_CALL_STATE value)
{
  switch (value)
    {
    case TOXAV_FRIEND_CALL_STATE_ERROR: debug_out << "TOXAV_FRIEND_CALL_STATE_ERROR"; return;
    case TOXAV_FRIEND_CALL_STATE_FINISHED: debug_out << "TOXAV_FRIEND_CALL_STATE_FINISHED"; return;
    case TOXAV_FRIEND_CALL_STATE_SENDING_A: debug_out << "TOXAV_FRIEND_CALL_STATE_SENDING_A"; return;
    case TOXAV_FRIEND_CALL_STATE_SENDING_V: debug_out << "TOXAV_FRIEND_CALL_STATE_SENDING_V"; return;
    case TOXAV_FRIEND_CALL_STATE_ACCEPTING_A: debug_out << "TOXAV_FRIEND_CALL_STATE_ACCEPTING_A"; return;
    case TOXAV_FRIEND_CALL_STATE_ACCEPTING_V: debug_out << "TOXAV_FRIEND_CALL_STATE_ACCEPTING_V"; return;
    }
  debug_out << "(TOXAV_FRIEND_CALL_STATE)" << value;
}

#endif
