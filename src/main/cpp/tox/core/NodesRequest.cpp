#include "NodesRequest.h"

using namespace tox;


NodesRequest::NodesRequest (PublicKey const &sender, Nonce const &nonce,
                            CryptoBox const &box,
                            PublicKey const &client_id, uint64_t ping_id)
  : DhtPacket (PacketKind::NodesRequest, sender, nonce,
               box,
               PlainText () << client_id << ping_id)
{
}
