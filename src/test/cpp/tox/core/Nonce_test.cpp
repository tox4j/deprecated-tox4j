#include "gtest/gtest.h"

#include "tox/core/Nonce.h"

using namespace tox;


TEST (Nonce, Random) {
  EXPECT_NE (Nonce::random (), Nonce::random ());
}


TEST (Nonce, Increment) {
  Nonce nonce;
  EXPECT_EQ (0, nonce[23]);
  ++nonce;
  EXPECT_EQ (1, nonce[23]);
}


TEST (Nonce, IncrementMany) {
  Nonce nonce;

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
