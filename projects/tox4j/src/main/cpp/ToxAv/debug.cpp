#include "ToxAv.h"

#ifdef TOXAV_VERSION_MAJOR

#include <algorithm>
#include <vector>

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

#endif
