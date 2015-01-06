#include "tox/core/CryptoBox.h"
#include "tox/core/Logging.h"
#include <gtest/gtest.h>

#include "tox/core/KeyPair.h"
#include "tox/core/Message.h"
#include "tox/core/Nonce.h"

using namespace tox;


static PlainText
mkPlainText (std::string const &str)
{
  return PlainText::from_string (str);
}

static PlainText
mkPlainText (byte_vector const &bytes)
{
  return PlainText::from_bytes (bytes);
}

static std::string
str (PlainText const &text)
{
  return std::string (text.begin (), text.end ());
}


TEST (CryptoBox, EmptyString) {
  KeyPair pair;
  CryptoBox box (pair);

  Nonce nonce;
  std::fill (nonce.begin (), nonce.end (), 0);

  Partial<PlainText> result = box.decrypt (box.encrypt (mkPlainText (""), nonce), nonce);
  EXPECT_EQ (Status::OK, result.code ());
  result >> [&](PlainText text) {
    EXPECT_EQ ("", str (text));
    return success ();
  };
}


TEST (CryptoBox, LongString) {
  KeyPair pair;
  CryptoBox box (pair);

  Nonce nonce;
  std::fill (nonce.begin (), nonce.end (), 0);

  // 10MiB of random data
  byte_vector original (10 * 1024 * 1024);
  randombytes_buf (original.data (), original.size ());

  Partial<PlainText> result = box.decrypt (box.encrypt (mkPlainText (original), nonce), nonce);
  EXPECT_EQ (Status::OK, result.code ());
  result >> [&](PlainText text) {
    EXPECT_EQ (std::string (original.begin (), original.end ()), str (text));
    return success ();
  };
}


TEST (CryptoBox, LongString0) {
  KeyPair pair;
  CryptoBox box (pair);

  Nonce nonce;
  std::fill (nonce.begin (), nonce.end (), 0);

  // 10MiB of zeroes
  byte_vector original (10 * 1024 * 1024);

  Partial<PlainText> result = box.decrypt (box.encrypt (mkPlainText (original), nonce), nonce);
  EXPECT_EQ (Status::OK, result.code ());
  result >> [&](PlainText text) {
    EXPECT_EQ (std::string (original.begin (), original.end ()), str (text));
    return success ();
  };
}


TEST (CryptoBox, EncryptDecrypt) {
  KeyPair pair;
  CryptoBox box (pair);

  Nonce nonce;
  std::fill (nonce.begin (), nonce.end (), 0);

  Partial<PlainText> result = box.decrypt (box.encrypt (mkPlainText ("hello"), nonce), nonce);
  EXPECT_EQ (Status::OK, result.code ());
  result >> [&](PlainText text) {
    EXPECT_EQ ("hello", str (text));
    return success ();
  };
}


TEST (CryptoBox, RandomNonce) {
  KeyPair pair;
  CryptoBox box (pair);

  Nonce nonce = Nonce::random ();
  std::fill (nonce.begin (), nonce.end (), 0);

  Partial<PlainText> result = box.decrypt (box.encrypt (mkPlainText ("hello"), nonce), nonce);
  EXPECT_EQ (Status::OK, result.code ());
  result >> [&](PlainText text) {
    EXPECT_EQ ("hello", str (text));
    return success ();
  };
}


TEST (CryptoBox, DecryptFailure) {
  KeyPair pair;
  CryptoBox box (pair);

  Nonce nonce;
  std::fill (nonce.begin (), nonce.end (), 0);

  CipherText encrypted = box.encrypt (mkPlainText ("hello"), nonce);

  // Make CipherText invalid.
  encrypted.data ()[0]++;

  Partial<PlainText> result = box.decrypt (encrypted, nonce);
  EXPECT_EQ (Status::HMACError, result.code ());
}


TEST (CryptoBox, BadNonce) {
  KeyPair pair;
  CryptoBox box (pair);

  Nonce nonce;
  std::fill (nonce.begin (), nonce.end (), 0);

  CipherText encrypted = box.encrypt (mkPlainText ("hello"), nonce);

  // Make nonce invalid.
  nonce[0]++;

  Partial<PlainText> result = box.decrypt (encrypted, nonce);
  EXPECT_EQ (Status::HMACError, result.code ());
}
