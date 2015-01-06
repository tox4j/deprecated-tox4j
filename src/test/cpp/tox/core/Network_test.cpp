#include "tox/core/Network.h"
#include "tox/core/Logging.h"
#include <gtest/gtest.h>

using namespace tox;


TEST (Network, ResolveEmpty) {
  Partial<NetworkAddress> addr = NetworkAddress::resolve ("");
  EXPECT_FALSE (addr.ok ());
}

TEST (Network, ResolveNull) {
  Partial<NetworkAddress> addr = NetworkAddress::resolve (nullptr);
  EXPECT_FALSE (addr.ok ());
}

TEST (Network, ResolveLocalhost) {
  Partial<NetworkAddress> addr = NetworkAddress::resolve ("localhost");
  EXPECT_TRUE (addr.ok ());
}

TEST (Network, Resolve127_0_0_1) {
  Partial<NetworkAddress> addr = NetworkAddress::resolve ("127.0.0.1");
  EXPECT_TRUE (addr.ok ());
}

TEST (Network, Resolve0_0_0_0) {
  Partial<NetworkAddress> addr = NetworkAddress::resolve ("0.0.0.0");
  EXPECT_TRUE (addr.ok ());
}

TEST (Network, Resolve255_255_255_255) {
  Partial<NetworkAddress> addr = NetworkAddress::resolve ("255.255.255.255");
  EXPECT_TRUE (addr.ok ());
}

TEST (Network, Resolve255_255_255_256) {
  Partial<NetworkAddress> addr = NetworkAddress::resolve ("255.255.255.256");
  EXPECT_FALSE (addr.ok ());
}
