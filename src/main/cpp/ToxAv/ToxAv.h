#include "tox4j/Tox4j.h"
#include "jniutil.h"

#define SUBSYSTEM TOXAV
#define CLASS     ToxAv
#define PREFIX    toxAv

namespace proto = im::tox::tox4j::av::proto;

using Subsystem = ToxAV;
using Events = tox_traits<Subsystem>::events;
