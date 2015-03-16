#include "ToxCore.h"


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
  assert (port >= 0);
  assert (port <= 65535);

  ByteArray public_key (env, publicKey);
  assert (!publicKey || public_key.size () == TOX_PUBLIC_KEY_SIZE);

  return with_instance (env, instanceNumber, "Bootstrap",
    [] (TOX_ERR_BOOTSTRAP error)
      {
        switch (error)
          {
          success_case (BOOTSTRAP);
          failure_case (BOOTSTRAP, NULL);
          failure_case (BOOTSTRAP, BAD_HOST);
          failure_case (BOOTSTRAP, BAD_PORT);
          }
        return unhandled ();
      },
    function, UTFChars (env, address).data (), port, public_key.data ()
  );
}


/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxBootstrap
 * Signature: (ILjava/lang/String;I[B)V
 */
TOX_METHOD (void, Bootstrap,
   jint instanceNumber, jstring address, jint port, jbyteArray publicKey)
{
  return toxBootstrapLike (tox_bootstrap, env, instanceNumber, address, port, publicKey);
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxAddTcpRelay
 * Signature: (ILjava/lang/String;I[B)V
 */
TOX_METHOD (void, AddTcpRelay,
   jint instanceNumber, jstring address, jint port, jbyteArray publicKey)
{
  return toxBootstrapLike (tox_add_tcp_relay, env, instanceNumber, address, port, publicKey);
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxGetUdpPort
 * Signature: (I)I
 */
TOX_METHOD (jint, GetUdpPort,
  jint instanceNumber)
{
  return with_instance (env, instanceNumber, "GetPort",
    [] (TOX_ERR_GET_PORT error)
      {
        switch (error)
          {
          success_case (GET_PORT);
          failure_case (GET_PORT, NOT_BOUND);
          }
        return unhandled ();
      },
    identity,
    tox_self_get_udp_port
  );
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxGetTcpPort
 * Signature: (I)I
 */
TOX_METHOD (jint, GetTcpPort,
  jint instanceNumber)
{
  return with_instance (env, instanceNumber, "GetPort",
    [] (TOX_ERR_GET_PORT error)
      {
        switch (error)
          {
          success_case (GET_PORT);
          failure_case (GET_PORT, NOT_BOUND);
          }
        return unhandled ();
      },
    identity,
    tox_self_get_tcp_port
  );
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxGetDhtId
 * Signature: (I)[B
 */
TOX_METHOD (jbyteArray, GetDhtId,
  jint instanceNumber)
{
  return with_instance (env, instanceNumber,
    GET_ARRAY (uint8_t, self, dht_id, TOX_PUBLIC_KEY_SIZE)
  );
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxIterationInterval
 * Signature: (I)I
 */
TOX_METHOD (jint, IterationInterval,
  jint instanceNumber)
{
  return with_instance (env, instanceNumber,
    [] (Tox const *tox, Events &events)
      {
        unused (events);
        return tox_iteration_interval (tox);
      }
  );
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxIteration
 * Signature: (I)[B
 */
TOX_METHOD (jbyteArray, Iteration,
  jint instanceNumber)
{
  return with_instance (env, instanceNumber,
    [=] (Tox *tox, Events &events)
      {
        tox_iterate (tox);

        std::vector<char> buffer (events.ByteSize ());
        events.SerializeToArray (buffer.data (), buffer.size ());
        events.Clear ();

        return toJavaArray (env, buffer);
      }
  );
}
