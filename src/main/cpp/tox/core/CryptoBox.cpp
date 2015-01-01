#include "CryptoBox.h"

#include <cassert>

#include <algorithm>

#include <sodium.h>


using namespace tox;


template<typename ForwardIterator>
static bool
is_all_zero (ForwardIterator begin, ForwardIterator end)
{
  return std::find_if (begin, end, [](byte zero) { return zero != 0; }) == end;
}

CryptoBox::CryptoBox (PublicKey const &public_key, SecretKey const &secret_key)
{
  crypto_box_beforenm (shared_key_.data (), public_key.data (), secret_key.data ());
}


CipherText
CryptoBox::encrypt (PlainText const &plain, Nonce const &n)
{
  byte_vector padded_plain (plain.size () + crypto_box_ZEROBYTES);
  std::copy (plain.begin (), plain.end (), padded_plain.begin () + crypto_box_ZEROBYTES);

  // The caller must ensure, before calling the C NaCl crypto_box function,
  // that the first crypto_box_ZEROBYTES bytes of the message m are all 0.
  assert (is_all_zero (padded_plain.cbegin (),
                       padded_plain.cbegin () + crypto_box_ZEROBYTES));

  // mlen counts all of the bytes, including the bytes required to be 0.
  size_t const mlen = plain.size () + crypto_box_ZEROBYTES;

  // The crypto_box function encrypts and authenticates a message.
  byte_vector padded_crypto (plain.size () + crypto_box_BOXZEROBYTES + crypto_box_MACBYTES);
  int result = crypto_box_afternm (padded_crypto.data (), padded_plain.data (), mlen,
                                  n.data (), shared_key_.data ());
  // It then returns 0.
  assert (result == 0);

  // The crypto_box function ensures that the first crypto_box_BOXZEROBYTES
  // bytes of the ciphertext c are all 0.
  assert (is_all_zero (padded_crypto.cbegin (),
                       padded_crypto.cbegin () + crypto_box_BOXZEROBYTES));

  return CipherText (padded_crypto.cbegin () + crypto_box_BOXZEROBYTES,
                     padded_crypto.cend ());
}


Partial<PlainText>
CryptoBox::decrypt (CipherText const &crypto, Nonce const &n)
{
  byte_vector padded_crypto (crypto.size () + crypto_box_BOXZEROBYTES);
  std::copy (crypto.begin (), crypto.end (), padded_crypto.begin () + crypto_box_BOXZEROBYTES);

  // The caller must ensure, before calling the crypto_box_open function,
  // that the first crypto_box_BOXZEROBYTES bytes of the ciphertext c are
  // all 0.
  assert (is_all_zero (padded_crypto.cbegin (),
                       padded_crypto.cbegin () + crypto_box_BOXZEROBYTES));

  // mlen counts all of the bytes, including the bytes required to be 0.
  size_t const mlen = crypto.size () + crypto_box_BOXZEROBYTES;

  byte_vector padded_plain (crypto.size () + crypto_box_ZEROBYTES);
  int result = crypto_box_open_afternm (padded_plain.data (), padded_crypto.data (), mlen,
                                       n.data (), shared_key_.data ());
  if (result != 0)
    return failure (Status::HMAC_ERROR);

  // The crypto_box_open function ensures (in case of success) that the first
  // crypto_box_ZEROBYTES bytes of the plaintext m are all 0.
  assert (is_all_zero (padded_plain.cbegin (),
                       padded_plain.cbegin () + crypto_box_ZEROBYTES));

  return success (padded_plain.cbegin () + crypto_box_ZEROBYTES,
                  padded_plain.cend () - crypto_box_MACBYTES);
}
