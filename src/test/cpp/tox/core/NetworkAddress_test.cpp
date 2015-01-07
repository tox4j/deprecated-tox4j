#include "tox/core/NetworkAddress.h"
#include "tox/core/Logging.h"
#include <gtest/gtest.h>

using namespace tox;


TEST (NetworkAddress, ResolveEmpty) {
  Partial<NetworkAddress> addr = NetworkAddress::resolve ("");
  EXPECT_FALSE (addr.ok ());
}

TEST (NetworkAddress, ResolveNull) {
  Partial<NetworkAddress> addr = NetworkAddress::resolve (nullptr);
  EXPECT_FALSE (addr.ok ());
}

TEST (NetworkAddress, ResolveLocalhost) {
  Partial<NetworkAddress> addr = NetworkAddress::resolve ("localhost");
  EXPECT_TRUE (addr.ok ());
}

TEST (NetworkAddress, Resolve127_0_0_1) {
  Partial<NetworkAddress> addr = NetworkAddress::resolve ("127.0.0.1");
  EXPECT_TRUE (addr.ok ());
}

TEST (NetworkAddress, Resolve0_0_0_0) {
  Partial<NetworkAddress> addr = NetworkAddress::resolve ("0.0.0.0");
  EXPECT_TRUE (addr.ok ());
}

TEST (NetworkAddress, Resolve255_255_255_255) {
  Partial<NetworkAddress> addr = NetworkAddress::resolve ("255.255.255.255");
  EXPECT_TRUE (addr.ok ());
}

TEST (NetworkAddress, Resolve255_255_255_256) {
  Partial<NetworkAddress> addr = NetworkAddress::resolve ("255.255.255.256");
  EXPECT_FALSE (addr.ok ());
}
