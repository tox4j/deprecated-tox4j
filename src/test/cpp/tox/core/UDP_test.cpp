#include "lwt/logging.h"
#include <gtest/gtest.h>

#include "tox/core/EchoRequest.h"
#include "tox/core/EchoResponse.h"
#include "tox/core/NodesRequest.h"
#include "tox/core/NodesResponse.h"
#include "tox/core/Nonce.h"
#include "tox/core/PacketHandler.h"

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


static PublicKey
parse_key (char const *str)
{
  auto unhex = [] (char c)
  {
    if (c >= '0' && c <= '9')
      return c - '0';
    if (c >= 'a' && c <= 'z')
      return c - 'a' + 10;
    if (c >= 'A' && c <= 'Z')
      return c - 'A' + 10;
    assert (false);
  };


  byte key[32];
  for (size_t i = 0; i < sizeof key; i++)
    {
      key[i] = unhex (str[i * 2    ]) << 4
             | unhex (str[i * 2 + 1]);
    }

  return PublicKey (key);
}


struct io_callback;

struct HandleEchoRequest
  : PacketHandler<HandleEchoRequest, EchoRequestFormat>
{
  io_callback &cb;

  HandleEchoRequest (io_callback &cb)
    : cb (cb)
  { }

  void done ();

  Partial<void> handle (PublicKey const &key,
                        Nonce const &nonce,
                        uint64_t ping_id)
  {
    LOG (INFO) << "Received EchoRequest packet";
    LOG (INFO) << "Key: " << key;
    LOG (INFO) << "Nonce: " << nonce;
    LOG (INFO) << "Ping ID: " << ping_id;
    done ();
    return success ();
  }
};


struct HandleEchoResponse
  : PacketHandler<HandleEchoResponse, EchoResponseFormat>
{
  io_callback &cb;

  HandleEchoResponse (io_callback &cb)
    : cb (cb)
  { }

  void done ();

  Partial<void> handle (PublicKey const &key,
                        Nonce const &nonce,
                        uint64_t ping_id)
  {
    LOG (INFO) << "Received EchoResponse packet";
    LOG (INFO) << "Key: " << key;
    LOG (INFO) << "Nonce: " << nonce;
    LOG (INFO) << "Ping ID: " << ping_id;
    done ();
    return success ();
  }
};


struct HandleNodesRequest
  : PacketHandler<HandleNodesRequest, NodesRequestFormat>
{
  io_callback &cb;

  HandleNodesRequest (io_callback &cb)
    : cb (cb)
  { }

  void done ();

  Partial<void> handle (PublicKey const &key,
                        Nonce const &nonce,
                        PublicKey const &requested_id,
                        uint64_t ping_id)
  {
    LOG (INFO) << "Received NodesRequest packet";
    LOG (INFO) << "Key: " << key;
    LOG (INFO) << "Nonce: " << nonce;
    LOG (INFO) << "Requested ID: " << requested_id;
    LOG (INFO) << "Ping ID: " << ping_id;
    done ();
    return success ();
  }
};


struct HandleNodesResponse
  : PacketHandler<HandleNodesResponse, NodesResponseFormat>
{
  io_callback &cb;

  HandleNodesResponse (io_callback &cb)
    : cb (cb)
  { }

  void done ();

  Partial<void> handle (PublicKey const &key,
                        Nonce const &nonce,
                        std::vector<
                          std::tuple<
                            variant<
                              std::tuple<Protocol, IPv4Address>,
                              std::tuple<Protocol, IPv6Address>
                            >,
                            uint16_t,
                            PublicKey
                          >
                        > &&nodes, uint64_t ping_id)
  {
    LOG (INFO) << "Received NodesResponse packet";
    LOG (INFO) << "Key: " << key;
    LOG (INFO) << "Nonce: " << nonce;
    LOG (INFO) << "Ping ID: " << ping_id;
    LOG (INFO) << "Node count: " << nodes.size ();
    for (auto node : nodes)
      {
        auto address   = std::get<0> (node);
        auto port      = std::get<1> (node);
        auto client_id = std::get<2> (node);
        address.match (
          [](std::tuple<Protocol, IPv4Address> address) {
            LOG (INFO) << "Protocol: " << std::get<0> (address);
            LOG (INFO) << "IPv4: " << std::get<1> (address);
          },

          [](std::tuple<Protocol, IPv6Address> address) {
            LOG (INFO) << "Protocol: " << std::get<0> (address);
            LOG (INFO) << "IPv6: " << std::get<1> (address);
          }
        );
        LOG (INFO) << "Port: " << port;
        LOG (INFO) << "Client ID: " << client_id;
      }
    done ();
    return success ();
  }
};


struct io_callback
{
  enum State
  {
    INIT,
    NODE_REQUEST,
    NODE_RESPONSE,
    ECHO_REQUEST,
    ECHO_RESPONSE,
  } state = INIT;

  PublicKey const requested_id = parse_key ("DA6B2411E6880C6CE25DA59E4163F70C6963108DD61E2C71D725E2F59F4C7B2F");

  int const sock;
  UniqueNonce nonces;
  PublicKey const &public_key;
  CryptoBox const &box;
  PacketDispatcher<> dispatcher;
  struct addrinfo *res;

  explicit io_callback (PublicKey const &public_key,
                        char const *ip, char const *port,
                        CryptoBox const &box)
    : sock (socket (AF_INET, SOCK_DGRAM, 0))
    , public_key (public_key)
    , box (box)
  {
    dispatcher.register_handler<HandleNodesRequest> (*this);
    dispatcher.register_handler<HandleNodesResponse> (*this);
    dispatcher.register_handler<HandleEchoRequest> (*this);
    dispatcher.register_handler<HandleEchoResponse> (*this);

    if (sock == -1)
      die ("socket");

    struct sockaddr_in addr;
    memset (&addr, 0, sizeof addr);
    addr.sin_family = AF_INET;
    addr.sin_port = htons (33445);
    addr.sin_addr.s_addr = INADDR_ANY;

    if (bind (sock, (struct sockaddr *) &addr, sizeof addr) != 0)
      die ("bind");

    struct addrinfo hints;
    memset (&hints, 0, sizeof (hints));
    hints.ai_family = AF_UNSPEC;
    hints.ai_socktype = SOCK_DGRAM;
    hints.ai_protocol = 0;
    hints.ai_flags = AI_ADDRCONFIG;

    if (getaddrinfo (ip, port, &hints, &res) != 0)
      die ("getaddrinfo");
    EXPECT_TRUE (res != nullptr);
  }


  void send_node_request ()
  {
    NodesRequest req (public_key, nonces.next (),
                      box,
                      requested_id, 1234);

    LOG (INFO) << "Sending node request";
    if (sendto (sock, req.data (), req.size (), 0, res->ai_addr, res->ai_addrlen) == -1)
      die ("sendto");
  }


  void send_echo_request ()
  {
    EchoRequest req (public_key, nonces.next (),
                     box,
                     1234);

    LOG (INFO) << "Sending echo request";
    if (sendto (sock, req.data (), req.size (), 0, res->ai_addr, res->ai_addrlen) == -1)
      die ("sendto");
  }


  void operator () (ev::io &w, int revents)
  {
    if (revents & EV_READ)
      {
        struct sockaddr src_addr;
        socklen_t addr_len;

        byte_vector packet_data (65507);

        ssize_t size = recvfrom (sock, packet_data.data (), packet_data.size (),
                                 0, &src_addr, &addr_len);
        assert (size > 0);

        LOG (INFO) << format ("events: EV_READ (%zd) [%02x]", size, packet_data.front ());

        auto success = dispatcher.handle (CipherText::from_bytes (packet_data, size), box);
        if (!success.ok ())
          LOG (WARNING) << "Unknown packet type: " << int (packet_data.front ());
      }
    if (revents & EV_WRITE)
      {
        if (state == INIT)
          {
            send_node_request ();
            state = NODE_REQUEST;
          }
        if (state == NODE_RESPONSE)
          {
            send_echo_request ();
            state = ECHO_REQUEST;
          }
        if (state == ECHO_RESPONSE)
          w.loop.break_loop ();
      }
  }
};


void HandleEchoRequest::done ()
{
}

void HandleEchoResponse::done ()
{
  cb.state = io_callback::ECHO_RESPONSE;
}

void HandleNodesRequest::done ()
{
}

void HandleNodesResponse::done ()
{
  cb.state = io_callback::NODE_RESPONSE;
}


TEST (UDP, NodesRequest) {
#if 1
  char const *key = "04119E835DF3E78BACF0F84235B300546AF8B936F035185E2A8E9E0A67C8924F";
  char const *ip = "144.76.60.215";
  char const *port = "33445";
#endif
#if 0
  char const *key = "F404ABAA1C99A9D37D61AB54898F56793E1DEF8BD46B1038B9D822E8460FAB67";
  char const *ip = "192.210.149.121";
  char const *port = "33445";
#endif

  ev::io io_watcher (ev::get_default_loop ());

  KeyPair self;
  CryptoBox box (parse_key (key), self.secret_key);

  io_callback cb (self.public_key, ip, port, box);

  io_watcher.set (cb.sock, EV_READ | EV_WRITE);
  io_watcher.set (&cb);
  io_watcher.start ();

  cb.nonces.randomise ();
  LOG (INFO) << "First nonce: " << cb.nonces.next ();

  LOG (INFO) << "Starting event loop";
  ev::get_default_loop ().run ();
}
