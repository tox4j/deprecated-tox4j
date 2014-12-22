#include <tox/av.h>
#include <tox/av_uncompat.h>

#include <tox/toxav.h>

#include "core_private.h"


struct new_ToxAV
{
  ToxAv *av;
  new_Tox *tox;

  new_ToxAV (ToxAv *av, new_Tox *tox)
    : av (av)
    , tox (tox)
  {
  }
};


new_ToxAV *
new_toxav_new (new_Tox *tox, TOXAV_ERR_NEW *error)
{
  if (!tox)
    {
      if (error) *error = TOXAV_ERR_NEW_NULL;
      return nullptr;
    }

  if (tox->has_av)
    {
      if (error) *error = TOXAV_ERR_NEW_MULTIPLE;
      return nullptr;
    }

  ToxAv *av = toxav_new (tox->tox, tox_count_friendlist (tox->tox) * 2 + 100);
  if (!av)
    {
      if (error) *error = TOXAV_ERR_NEW_MALLOC;
      return nullptr;
    }

  tox->has_av = true;

  if (error) *error = TOXAV_ERR_NEW_MALLOC;
  return new new_ToxAV (av, tox);
}

void
new_toxav_kill (new_ToxAV *av)
{
  toxav_kill (av->av);
  av->tox->has_av = false;
  delete av;
}
