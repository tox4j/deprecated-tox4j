#include "tox/core/Nonce.h"
#include "tox/core/Logging.h"
#include <gtest/gtest.h>

using namespace tox;


TEST (Nonce, Random) {
  EXPECT_NE (Nonce::random (), Nonce::random ());
}


TEST (Nonce, Increment) {
  Nonce nonce { };
  EXPECT_EQ (0, nonce[23]);
  ++nonce;
  EXPECT_EQ (1, nonce[23]);
}


TEST (Nonce, IncrementMany) {
  Nonce nonce { };

  for (int i = 0; i < 255; i++)
    ++nonce;
  EXPECT_EQ (255, nonce[23]);

  ++nonce;
  EXPECT_EQ (0, nonce[23]);
  EXPECT_EQ (1, nonce[22]);

  ++nonce;
  EXPECT_EQ (1, nonce[23]);
  EXPECT_EQ (1, nonce[22]);
}


TEST (UniqueNonce, Next) {
  UniqueNonce nonces;

  for (int i = 0; i < 255; i++)
    nonces.next ();
  EXPECT_EQ (255, nonces.next ()[23]);

  Nonce nonce = nonces.next ();
  EXPECT_EQ (0, nonce[23]);
  EXPECT_EQ (1, nonce[22]);
}
