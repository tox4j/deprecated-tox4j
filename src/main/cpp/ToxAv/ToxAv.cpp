#ifdef HAVE_TOXAV
#include "ToxAv.h"

using namespace av;


ToxInstances<tox::av_ptr, std::unique_ptr<Events>> av::instances;

template<>
extern char const *const module_name<ToxAV> = "av";
#endif
