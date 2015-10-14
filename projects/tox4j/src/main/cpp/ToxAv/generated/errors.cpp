#include "../ToxAv.h"

#ifdef TOXAV_VERSION_MAJOR

HANDLE ("Answer", ANSWER)
{
  switch (error)
    {
    success_case (ANSWER);
    failure_case (ANSWER, CODEC_INITIALIZATION);
    failure_case (ANSWER, FRIEND_NOT_CALLING);
    failure_case (ANSWER, FRIEND_NOT_FOUND);
    failure_case (ANSWER, INVALID_BIT_RATE);
    }
  return unhandled ();
}

HANDLE ("CallControl", CALL_CONTROL)
{
  switch (error)
    {
    success_case (CALL_CONTROL);
    failure_case (CALL_CONTROL, FRIEND_NOT_FOUND);
    failure_case (CALL_CONTROL, FRIEND_NOT_IN_CALL);
    failure_case (CALL_CONTROL, INVALID_TRANSITION);
    }
  return unhandled ();
}

HANDLE ("Call", CALL)
{
  switch (error)
    {
    success_case (CALL);
    failure_case (CALL, FRIEND_ALREADY_IN_CALL);
    failure_case (CALL, FRIEND_NOT_CONNECTED);
    failure_case (CALL, FRIEND_NOT_FOUND);
    failure_case (CALL, INVALID_BIT_RATE);
    failure_case (CALL, MALLOC);
    }
  return unhandled ();
}

HANDLE ("New", NEW)
{
  switch (error)
    {
    success_case (NEW);
    failure_case (NEW, MALLOC);
    failure_case (NEW, MULTIPLE);
    failure_case (NEW, NULL);
    }
  return unhandled ();
}

HANDLE ("SendFrame", SEND_FRAME)
{
  switch (error)
    {
    success_case (SEND_FRAME);
    failure_case (SEND_FRAME, FRIEND_NOT_FOUND);
    failure_case (SEND_FRAME, FRIEND_NOT_IN_CALL);
    failure_case (SEND_FRAME, INVALID);
    failure_case (SEND_FRAME, NULL);
    failure_case (SEND_FRAME, PAYLOAD_TYPE_DISABLED);
    failure_case (SEND_FRAME, RTP_FAILED);
    }
  return unhandled ();
}

HANDLE ("SetBitRate", SET_BIT_RATE)
{
  switch (error)
    {
    success_case (SET_BIT_RATE);
    failure_case (SET_BIT_RATE, FRIEND_NOT_FOUND);
    failure_case (SET_BIT_RATE, FRIEND_NOT_IN_CALL);
    failure_case (SET_BIT_RATE, INVALID);
    }
  return unhandled ();
}

#endif
