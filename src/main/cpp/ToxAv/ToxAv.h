#include "tox4j/Tox4j.h"
#include "jniutil.h"

#define SUBSYSTEM TOXAV
#define CLASS     ToxAv
#define PREFIX    toxav

namespace proto = im::tox::tox4j::av::proto;

using proto::AvEvents;

extern instance_manager<tox::av_ptr, AvEvents> av;
