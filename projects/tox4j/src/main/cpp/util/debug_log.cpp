#include "util/debug_log.h"

#include <tox/core.h>

#include <cctype>
#include <iostream>
#include <map>

bool output_data_pointer = false;
std::ostream &debug_out = std::cout;

#undef register_func
#define register_func(func) std::make_pair (reinterpret_cast<uintptr_t> (func), #func)

static std::map<uintptr_t, char const *> &
func_names ()
{
  static std::map<uintptr_t, char const *> func_names;
  return func_names;
}

#undef register_func
bool
register_func (uintptr_t func, char const *name)
{
  auto &names = func_names ();
  assert (names.find (func) == names.end ());
  names.insert (std::make_pair (func, name));
  return true;
}

void
print_func (uintptr_t func)
{
  auto &names = func_names ();
  auto found = names.find (func);
  if (found != names.end ())
    debug_out << found->second;
  else
    debug_out << func;
}

template<typename Arg>
void
print_arg (Arg arg)
{
  debug_out << arg;
}

template void print_arg<char const *      > (char const *      );
template void print_arg<bool              > (bool              );
template void print_arg<  signed char     > (  signed char     );
template void print_arg<unsigned char     > (unsigned char     );
template void print_arg<  signed int      > (  signed int      );
template void print_arg<unsigned int      > (unsigned int      );
template void print_arg<  signed long     > (  signed long     );
template void print_arg<unsigned long     > (unsigned long     );
template void print_arg<  signed long long> (  signed long long);
template void print_arg<unsigned long long> (unsigned long long);

template<>
void
print_arg<uint8_t *> (uint8_t *data)
{
  (void)data;
  debug_out << "in data";
}

template<>
void
print_arg<uint8_t const *> (uint8_t const *data)
{
  (void)data;
  debug_out << "out data";
}

void
print_arg (unsigned char const *data, std::size_t length)
{
  (void)data;
  debug_out << "out data[" << length << "]";
}
