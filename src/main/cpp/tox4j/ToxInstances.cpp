#include "tox4j/Tox4j.h"

template<typename Subsystem, typename Traits>
instance_manager<Subsystem, Traits>
instance_manager<Subsystem, Traits>::self;

template instance_manager<Tox> instance_manager<Tox>::self;
