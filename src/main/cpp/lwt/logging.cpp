#include "logging.h"

#include <iostream>
#include <iomanip>
#include <iterator>

using namespace tox;


void
tox::output_hex (std::ostream &os, uint8_t const *data, size_t length)
{
  os << '[';
  for (size_t i = 0; i < length; i++)
    os << format ("%02x", data[i]);
  os << ']';
}


std::ostream &
tox::operator << (std::ostream &os, formatter const &fmt)
{
  std::copy (fmt.text_.cbegin (), fmt.text_.cend (), std::ostream_iterator<char> (os));
  return os;
}
