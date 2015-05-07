#include "ToxAv.h"

using namespace av;


ToxInstances<tox::av_ptr, Events> av::instances;

template<>
extern char const *const module_name<ToxAv> = "av";
