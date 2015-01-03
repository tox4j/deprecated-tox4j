#include "Logging.h"

#include <iostream>
#include <iomanip>
#include <iterator>

using namespace tox;


void
tox::output_hex (std::ostream &os, byte const *data, size_t length)
{
  os << '[';
  os << std::setfill ('0') << std::setw (2) << std::hex;
  for (size_t i = 0; i < length; i++)
    os << int (data[i]);
  os << ']';
}


std::ostream &
tox::operator << (std::ostream &os, formatter const &fmt)
{
  std::copy (fmt.text_.cbegin (), fmt.text_.cend (), std::ostream_iterator<char> (os));
  return os;
}
