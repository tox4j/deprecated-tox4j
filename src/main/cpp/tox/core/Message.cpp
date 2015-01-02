#include "Message.h"

using namespace tox;


PlainText &
PlainText::operator << (byte b)
{
  push_back (b);
  return *this;
}


PlainText &
PlainText::operator << (PlainText const &plain)
{
  append (plain.cbegin (), plain.cend ());
  return *this;
}


PlainText &
PlainText::operator << (PublicKey const &key)
{
  append (key.cbegin (), key.cend ());
  return *this;
}


PlainText &
PlainText::operator << (Nonce const &nonce)
{
  append (nonce.cbegin (), nonce.cend ());
  return *this;
}
