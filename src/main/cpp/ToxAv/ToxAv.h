#include "tox4j/Tox4j.h"
#include "jniutil.h"

#define SUBSYSTEM TOXAV

namespace proto = av::proto;

using av::Events;
using av::tox_traits;
using av::with_instance;
using av::with_error_handling;
