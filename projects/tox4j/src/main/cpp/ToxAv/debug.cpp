#include "ToxAv.h"

#include <algorithm>
#include <vector>

#ifdef TOXAV_VERSION_MAJOR

template<>
void
print_arg<ToxAV *> (ToxAV *tox)
{
  static std::vector<ToxAV *> ids;
  auto found = std::find (ids.begin (), ids.end (), tox);
  if (found == ids.end ())
    {
      ids.push_back (tox);
      found = ids.end () - 1;
    }
  debug_out << "@" << (found - ids.begin () + 1);
}

template<>
void
print_arg<int16_t const *> (int16_t const *data)
{
  if (data != nullptr)
    debug_out << "out audio samples";
  else
    debug_out << "<null>";
}

#define enum_case(ENUM)                               \
    case TOXAV_##ENUM: debug_out << "TOXAV_" #ENUM; break

template<>
void
print_arg<TOXAV_CALL_CONTROL> (TOXAV_CALL_CONTROL kind)
{
  switch (kind)
    {
    enum_case (CALL_CONTROL_PAUSE);
    enum_case (CALL_CONTROL_RESUME);
    enum_case (CALL_CONTROL_CANCEL);
    enum_case (CALL_CONTROL_MUTE_AUDIO);
    enum_case (CALL_CONTROL_UNMUTE_AUDIO);
    enum_case (CALL_CONTROL_HIDE_VIDEO);
    enum_case (CALL_CONTROL_SHOW_VIDEO);
    }
}

#endif
