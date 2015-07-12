#include "ToxAv.h"

#ifdef TOXAV_VERSION_MAJOR

using namespace av;

static PP_UNUSED void
reference_symbols ()
{
  int toxav_finalize; // For Java only.
#include "generated/natives.h"
}

ToxInstances<tox::av_ptr, std::unique_ptr<Events>> av::instances;

template<> extern char const *const module_name<ToxAV> = "av";
template<> extern char const *const exn_prefix<ToxAV> = "av";

#endif
