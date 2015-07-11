#pragma once

#include "tox/common.h"
#include <tox/tox.h>


namespace tox
{
  struct core_deleter
  {
    void operator () (Tox *tox)
    {
      tox_kill (tox);
    }
  };

  typedef std::unique_ptr<Tox, core_deleter> core_ptr;

#define CALLBACK(NAME)  using callback_##NAME = detail::cb<Tox, tox_##NAME##_cb, tox_callback_##NAME>;
#include "generated/core.h"
#undef CALLBACK
}
