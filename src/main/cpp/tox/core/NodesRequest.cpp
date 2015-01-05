#include "NodesRequest.h"
#include "Logging.h"

using namespace tox;


NodesRequest::NodesRequest (PublicKey const &sender, Nonce const &nonce,
                            CryptoBox const &box,
                            PublicKey const &client_id, uint64_t ping_id)
  : Packet<NodesRequestFormat> (sender, nonce, box, client_id, ping_id)
{
  size_t const expected_size = 1 + crypto_box_PUBLICKEYBYTES + crypto_box_NONCEBYTES + crypto_box_PUBLICKEYBYTES + sizeof ping_id + crypto_box_MACBYTES;
  assert (size () == expected_size);
}
