#include "ToxCore.h"

#ifdef TOX_VERSION_MAJOR

using namespace core;


ToxInstances<tox::core_ptr, std::unique_ptr<Events>> core::instances;

template<> extern char const *const module_name<Tox> = "core";
template<> extern char const *const exn_prefix<Tox> = "";

#endif
