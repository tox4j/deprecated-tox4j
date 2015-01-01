#include "gtest/gtest.h"

#include "tox/core/CryptoBox.h"

using namespace tox;


TEST (CryptoBox, EncryptDecrypt) {
  KeyPair pair;
  CryptoBox box (pair);

  Nonce nonce;
  std::fill (nonce.begin (), nonce.end (), 0);

  Partial<PlainText> result = box.decrypt (box.encrypt (PlainText ("hello"), nonce), nonce);
  EXPECT_EQ (Status::OK, result.code ());
  PlainText text = result.value ();
  EXPECT_EQ ("hello", text.str ());
}


TEST (CryptoBox, RandomNonce) {
  KeyPair pair;
  CryptoBox box (pair);

  Nonce nonce = Nonce::random ();
  std::fill (nonce.begin (), nonce.end (), 0);

  Partial<PlainText> result = box.decrypt (box.encrypt (PlainText ("hello"), nonce), nonce);
  EXPECT_EQ (Status::OK, result.code ());
  PlainText text = result.value ();
  EXPECT_EQ ("hello", text.str ());
}


TEST (CryptoBox, DecryptFailure) {
  KeyPair pair;
  CryptoBox box (pair);

  Nonce nonce;
  std::fill (nonce.begin (), nonce.end (), 0);

  CipherText encrypted = box.encrypt (PlainText ("hello"), nonce);

  // Make CipherText invalid.
  encrypted[0]++;

  Partial<PlainText> result = box.decrypt (encrypted, nonce);
  EXPECT_EQ (Status::HMAC_ERROR, result.code ());
}


TEST (CryptoBox, BadNonce) {
  KeyPair pair;
  CryptoBox box (pair);

  Nonce nonce;
  std::fill (nonce.begin (), nonce.end (), 0);

  CipherText encrypted = box.encrypt (PlainText ("hello"), nonce);

  // Make nonce invalid.
  nonce[0]++;

  Partial<PlainText> result = box.decrypt (encrypted, nonce);
  EXPECT_EQ (Status::HMAC_ERROR, result.code ());
}
