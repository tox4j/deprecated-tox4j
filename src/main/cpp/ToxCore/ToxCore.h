#include "tox4j/Tox4j.h"
#include "jniutil.h"

#define SUBSYSTEM TOX
#define CLASS     ToxCore
#define PREFIX    tox

namespace proto = im::tox::tox4j::core::proto;

using Subsystem = Tox;
using Events = tox_traits<Subsystem>::events;
