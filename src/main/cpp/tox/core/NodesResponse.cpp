#include "NodesResponse.h"
#include "Logging.h"

using namespace tox;


PlainText &
tox::operator << (PlainText &packet, Protocol protocol)
{
  return packet << static_cast<byte> (protocol);
}


NodesResponse::NodesResponse (PublicKey const &sender, Nonce const &nonce,
                              CryptoBox const &box,
                              std::vector<
                                std::tuple<
                                  variant<
                                    std::tuple<Protocol, IPv4Address>,
                                    std::tuple<Protocol, IPv6Address>
                                  >,
                                  unsigned short,
                                  PublicKey
                                >
                              > const &nodes,
                              uint64_t ping_id)
  : Packet<NodesResponseFormat> (sender, nonce, box, nonce, nodes, ping_id)
{
}
