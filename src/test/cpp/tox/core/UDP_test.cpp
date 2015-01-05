#include "tox/core/Logging.h"
#include <gtest/gtest.h>

#include "tox/core/NodesRequest.h"
#include "tox/core/NodesResponse.h"
#include "tox/core/Nonce.h"

#include <ev++.h>

#include <netdb.h>
#include <netinet/in.h>
#include <resolv.h>
#include <sys/socket.h>
#include <unistd.h>

using namespace tox;


static void
die (char const *func)
{
  perror (func);
  exit (1);
}


struct io_callback
{
  int sock;
  CryptoBox const &box;

  io_callback (CryptoBox const &box)
    : sock (socket (AF_INET, SOCK_DGRAM, 0))
    , box (box)
  {
    if (sock == -1)
      die ("socket");

    struct sockaddr_in addr;
    memset (&addr, 0, sizeof addr);
    addr.sin_family = AF_INET;
    addr.sin_port = htons (33445);
    addr.sin_addr.s_addr = INADDR_ANY;

    if (bind (sock, (struct sockaddr *) &addr, sizeof addr) != 0)
      die ("bind");
  }

  void handle_packet (CipherText const &packet)
  {
    assert (packet.size () > 0);
    assert (packet.data ()[0] == 0x04);
    //NodesResponse::decode (packet, box);
  }

  void operator() (ev::io &w, int revents)
  {
    switch (revents)
      {
      case EV_READ:
        {
          struct sockaddr src_addr;
          socklen_t addr_len;

          byte_vector packet_data (65507);

          ssize_t size = recvfrom (sock, packet_data.data (), packet_data.size (),
                                   0, &src_addr, &addr_len);

          LOG (INFO) << format ("events: EV_READ (%zd) [%02x]", size, packet_data.front ());

          handle_packet (CipherText::from_bytes (packet_data, size));
          break;
        }
      case EV_WRITE:
        printf ("events: EV_WRITE\n");
        break;
      case EV_READ | EV_WRITE:
        printf ("events: EV_READ | EV_WRITE\n");
        break;
      }
  }
};


static int
unhex (char c)
{
  if (c >= '0' && c <= '9')
    return c - '0';
  if (c >= 'a' && c <= 'z')
    return c - 'a' + 10;
  if (c >= 'A' && c <= 'Z')
    return c - 'A' + 10;
  assert (false);
}


static PublicKey
parse_key (char const *str)
{
  byte key[32];
  for (size_t i = 0; i < sizeof key; i++)
    {
      key[i] = unhex (str[i * 2    ]) << 4
             | unhex (str[i * 2 + 1]);
    }

  return PublicKey (key);
}


TEST (NodesRequest, GetNodes) {
#if 0
  char const *key = "04119E835DF3E78BACF0F84235B300546AF8B936F035185E2A8E9E0A67C8924F";
  char const *ip = "144.76.60.215";
  char const *port = "33445";
#endif
#if 1
  char const *key = "F404ABAA1C99A9D37D61AB54898F56793E1DEF8BD46B1038B9D822E8460FAB67";
  char const *ip = "192.210.149.121";
  char const *port = "33445";
#endif

  ev::io loop;

  KeyPair self;
  PublicKey bootstrap_node = parse_key (key);
  PublicKey client_id      = parse_key ("DA6B2411E6880C6CE25DA59E4163F70C6963108DD61E2C71D725E2F59F4C7B2F");

  CryptoBox box (bootstrap_node, self.secret_key);

  io_callback cb (box);

  loop.set (cb.sock, EV_READ);
  loop.set (&cb);
  loop.start ();

  UniqueNonce nonces;
  LOG (INFO) << "First nonce: " << nonces.next ();

  NodesRequest req (self.public_key, nonces.next (),
                    box,
                    client_id, 1234);

  struct addrinfo hints;
  memset (&hints, 0, sizeof (hints));
  hints.ai_family = AF_UNSPEC;
  hints.ai_socktype = SOCK_DGRAM;
  hints.ai_protocol = 0;
  hints.ai_flags = AI_ADDRCONFIG;

  struct addrinfo *res;
  if (getaddrinfo (ip, port, &hints, &res) != 0)
    die ("getaddrinfo");
  EXPECT_TRUE (res != nullptr);

  if (sendto (cb.sock, req.data (), req.size (), 0, res->ai_addr, res->ai_addrlen) == -1)
    die ("sendto");

  LOG (INFO) << "Starting event loop";
  ev::get_default_loop ().run ();
}
