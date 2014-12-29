#include "tox4j/Tox4j.h"
#include "jniutil.h"

#define SUBSYSTEM TOX

namespace proto = core::proto;

using core::Events;
using core::tox_traits;
using core::with_instance;
using core::with_error_handling;
