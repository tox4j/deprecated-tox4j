#pragma once

#include <sstream>


/**
 * Convert any object to string using operator<<(ostream&, T).
 *
 * This function is a temporary implementation of std::to_string provided until
 * C++ standard library implementations catch up.
 */
template<typename T>
std::string
to_string (T const &v)
{
  std::ostringstream out;

  out << v;
  return out.str ();
}
