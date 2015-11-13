#include "ToxAv.h"

#ifdef TOXAV_VERSION_MAJOR

#include <algorithm>
#include <vector>

template<>
void
print_arg<ToxAV *> (protolog::Value &value, ToxAV *tox)
{
  static std::vector<ToxAV *> ids;
  auto found = std::find (ids.begin (), ids.end (), tox);
  if (found == ids.end ())
    {
      ids.push_back (tox);
      found = ids.end () - 1;
    }
  value.set_string ("@" + std::to_string (found - ids.begin () + 1));
}

template<>
void
print_arg<int16_t const *> (protolog::Value &value, int16_t const *data)
{
  if (data != nullptr)
    value.set_string ("out audio samples");
  else
    value.set_string ("<null>");
}

template<>
void
print_arg<av::Events *> (protolog::Value &value, av::Events *events)
{
  if (events != nullptr)
    value.set_string ("<core::Events[" + std::to_string (events->ByteSize ()) + "]>");
  else
    value.set_string ("<null>");
}

#endif
