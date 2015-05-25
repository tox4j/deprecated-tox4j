#include "ToxCore.h"

using namespace core;


static void
toxBootstrapLike (bool function (Tox *tox,
                                 char const *host,
                                 uint16_t port,
                                 uint8_t const *public_key,
                                 TOX_ERR_BOOTSTRAP *error),
                  JNIEnv *env,
                  jint instanceNumber,
                  jstring address,
                  jint port,
                  jbyteArray publicKey)
{
  tox4j_assert (port >= 0);
  tox4j_assert (port <= 65535);

  ByteArray public_key (env, publicKey);
  tox4j_assert (!publicKey || public_key.size () == TOX_PUBLIC_KEY_SIZE);

  return instances.with_instance_ign (env, instanceNumber, "Bootstrap",
    function, UTFChars (env, address).data (), port, public_key.data ()
  );
}


/*
 * Class:     im_tox_tox4j_impl_ToxCoreJni
 * Method:    toxBootstrap
 * Signature: (ILjava/lang/String;I[B)V
 */
TOX_METHOD (void, Bootstrap,
   jint instanceNumber, jstring address, jint port, jbyteArray publicKey)
{
  return toxBootstrapLike (tox_bootstrap, env, instanceNumber, address, port, publicKey);
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreJni
 * Method:    toxAddTcpRelay
 * Signature: (ILjava/lang/String;I[B)V
 */
TOX_METHOD (void, AddTcpRelay,
   jint instanceNumber, jstring address, jint port, jbyteArray publicKey)
{
  return toxBootstrapLike (tox_add_tcp_relay, env, instanceNumber, address, port, publicKey);
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreJni
 * Method:    toxGetUdpPort
 * Signature: (I)I
 */
TOX_METHOD (jint, GetUdpPort,
  jint instanceNumber)
{
  return instances.with_instance_err (env, instanceNumber, "GetPort",
    identity,
    tox_self_get_udp_port
  );
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreJni
 * Method:    toxGetTcpPort
 * Signature: (I)I
 */
TOX_METHOD (jint, GetTcpPort,
  jint instanceNumber)
{
  return instances.with_instance_err (env, instanceNumber, "GetPort",
    identity,
    tox_self_get_tcp_port
  );
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreJni
 * Method:    toxGetDhtId
 * Signature: (I)[B
 */
TOX_METHOD (jbyteArray, GetDhtId,
  jint instanceNumber)
{
  return instances.with_instance (env, instanceNumber,
    [env] (Tox const *tox, Events &events)
      {
        unused (events);
        return get_array<uint8_t, TOX_PUBLIC_KEY_SIZE,
          tox_self_get_dht_id> (env, tox);
      }
  );
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreJni
 * Method:    toxIterationInterval
 * Signature: (I)I
 */
TOX_METHOD (jint, IterationInterval,
  jint instanceNumber)
{
  return instances.with_instance_noerr (env, instanceNumber,
    tox_iteration_interval);
}

/*
 * Class:     im_tox_tox4j_impl_ToxCoreJni
 * Method:    toxIteration
 * Signature: (I)[B
 */
TOX_METHOD (jbyteArray, Iteration,
  jint instanceNumber)
{
  return instances.with_instance (env, instanceNumber,
    [=] (Tox *tox, Events &events) -> jbyteArray
      {
        tox_iterate (tox);
        if (events.ByteSize () == 0)
          return nullptr;

        std::vector<char> buffer (events.ByteSize ());
        events.SerializeToArray (buffer.data (), buffer.size ());
        events.Clear ();

        return toJavaArray (env, buffer);
      }
  );
}
