#pragma once

#include <tox/toxav.h>


#include <memory>
#include <cassert>


namespace tox
{
  struct av_deleter
  {
    void operator () (ToxAv *toxav)
    {
      toxav_kill (toxav);
    }
  };

  typedef std::unique_ptr<ToxAv, av_deleter> av_ptr;
}
