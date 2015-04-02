#include "ToxCore.h"

/*
 * Class:     im_tox_tox4j_ToxCoreImpl
 * Method:    playground
 * Signature: (I)V
 */
METHOD (void, playground,
  jint instance_number)
{
  return with_instance (env, instance_number,
    [=] (Tox *tox, Events &events)
      {
        unused (tox);
        unused (events);
      }
  );
}
