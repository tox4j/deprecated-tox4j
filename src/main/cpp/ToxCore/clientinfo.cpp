#include "ToxCore.h"

using namespace core;


/*
 * Class:     im_tox_tox4j_impl_ToxCoreNative
 * Method:    toxSelfGetPublicKey
 * Signature: (I)[B
 */
TOX_METHOD (jbyteArray, SelfGetPublicKey,
  jint instanceNumber)
{
  return instances.with_instance (env, instanceNumber,
    [env] (Tox const *tox, Events &events)
      {
        unused (events);
        return get_array<uint8_t, TOX_PUBLIC_KEY_SIZE,
          tox_self_get_public_key> (env, tox);
      }
  );
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreNative
 * Method:    toxSelfGetSecretKey
 * Signature: (I)[B
 */
TOX_METHOD (jbyteArray, SelfGetSecretKey,
  jint instanceNumber)
{
  return instances.with_instance (env, instanceNumber,
    [env] (Tox const *tox, Events &events)
      {
        unused (events);
        return get_array<uint8_t, TOX_SECRET_KEY_SIZE,
          tox_self_get_secret_key> (env, tox);
      }
  );
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreNative
 * Method:    toxSelfSetNospam
 * Signature: (II)V
 */
TOX_METHOD (void, SelfSetNospam,
  jint instanceNumber, jint nospam)
{
  return instances.with_instance_noerr (env, instanceNumber,
    tox_self_set_nospam, nospam);
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreNative
 * Method:    toxSelfGetNospam
 * Signature: (I)I
 */
TOX_METHOD (jint, SelfGetNospam,
  jint instanceNumber)
{
  return instances.with_instance_noerr (env, instanceNumber,
    tox_self_get_nospam);
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreNative
 * Method:    toxSelfGetAddress
 * Signature: (I)[B
 */
TOX_METHOD (jbyteArray, SelfGetAddress,
  jint instanceNumber)
{
  return instances.with_instance (env, instanceNumber,
    [env] (Tox const *tox, Events &events)
      {
        unused (events);
        return get_array<uint8_t, TOX_ADDRESS_SIZE,
          tox_self_get_address> (env, tox);
      }
  );
}


/*
 * Class:     im_tox_tox4j_impl_ToxCoreNative
 * Method:    toxSelfSetName
 * Signature: (I[B)V
 */
TOX_METHOD (void, SelfSetName,
  jint instanceNumber, jbyteArray name)
{
  ByteArray name_array (env, name);
  return instances.with_instance_ign (env, instanceNumber, "SetInfo",
    tox_self_set_name, name_array.data (), name_array.size ());
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreNative
 * Method:    toxSelfGetName
 * Signature: (I)[B
 */
TOX_METHOD (jbyteArray, SelfGetName,
  jint instanceNumber)
{
  return instances.with_instance (env, instanceNumber,
    [env] (Tox const *tox, Events &events)
      {
        unused (events);
        return get_vector<uint8_t,
          tox_self_get_name_size,
          tox_self_get_name> (env, tox);
      }
  );
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreNative
 * Method:    toxSelfSetStatusMessage
 * Signature: (I[B)V
 */
TOX_METHOD (void, SelfSetStatusMessage,
  jint instanceNumber, jbyteArray statusMessage)
{
  ByteArray status_message_array (env, statusMessage);
  return instances.with_instance_ign (env, instanceNumber, "SetInfo",
    tox_self_set_status_message, status_message_array.data (), status_message_array.size ());
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreNative
 * Method:    toxSelfGetStatusMessage
 * Signature: (I)[B
 */
TOX_METHOD (jbyteArray, SelfGetStatusMessage,
  jint instanceNumber)
{
  return instances.with_instance (env, instanceNumber,
    [env] (Tox const *tox, Events &events)
      {
        unused (events);
        return get_vector<uint8_t,
          tox_self_get_status_message_size,
          tox_self_get_status_message> (env, tox);
      }
  );
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreNative
 * Method:    toxSelfSetStatus
 * Signature: (II)V
 */
TOX_METHOD (void, SelfSetStatus,
  jint instanceNumber, jint status)
{
  TOX_USER_STATUS const status_enum = [=] {
    switch (status)
      {
      case 0: return TOX_USER_STATUS_NONE;
      case 1: return TOX_USER_STATUS_AWAY;
      case 2: return TOX_USER_STATUS_BUSY;
      }
    tox4j_fatal ("Invalid user status from Java");
  } ();

  return instances.with_instance_noerr (env, instanceNumber,
    tox_self_set_status, status_enum);
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreNative
 * Method:    toxSelfGetStatus
 * Signature: (I)I
 */
TOX_METHOD (jint, SelfGetStatus,
  jint instanceNumber)
{
  switch (instances.with_instance_noerr (env, instanceNumber, tox_self_get_status))
    {
    case TOX_USER_STATUS_NONE: return 0;
    case TOX_USER_STATUS_AWAY: return 1;
    case TOX_USER_STATUS_BUSY: return 2;
    }
  tox4j_fatal ("Invalid result from tox_self_get_status");
}
