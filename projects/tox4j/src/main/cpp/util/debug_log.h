#ifndef DEBUG_LOG_H
#define DEBUG_LOG_H

#include "util/pp_attributes.h"
#include "util/pp_cat.h"

#include <ostream>

extern std::ostream &debug_out;

void print_arg (uint8_t const *data, std::size_t length);

template<typename Arg>
void print_arg (Arg arg);

static inline void
print_args ()
{ }

template<typename Arg0, typename ...Args>
void print_args (Arg0 arg0, Args ...args);

template<typename ...Args>
void
print_args (uint8_t const *data, std::size_t size, Args ...args)
{
  print_arg (data, size);
  if (sizeof... (Args))
    debug_out << ", ";
  print_args (args...);
}

template<typename Arg0, typename ...Args>
void
print_args (Arg0 arg0, Args ...args)
{
  print_arg (arg0);
  if (sizeof... (Args))
    debug_out << ", ";
  print_args (args...);
}

void print_func (uintptr_t func);

template<typename Func, typename ...Args>
void
debug_log (Func func, Args ...args)
{
  print_func (reinterpret_cast<uintptr_t> (func));
  debug_out << "(";
  print_args (args...);
  debug_out << ")" << std::endl;
}

template<typename Func, typename ...Args>
void
debug_log (int instanceNumber, Func func, Args ...args)
{
  print_func (reinterpret_cast<uintptr_t> (func));
  debug_out << "[#" << instanceNumber;
  debug_out << "](";
  print_args (args...);
  debug_out << ")" << std::endl;
}

bool register_func (uintptr_t func, char const *name);

static inline bool
register_funcs ()
{
  return true;
}

template<typename Func, typename Name, typename ...Funcs>
bool
register_funcs (Func func, Name name, Funcs... funcs)
{
  register_func (func, name);
  return register_funcs (funcs...);
}

#define register_funcs static PP_UNUSED bool PP_CAT (register_funcs_, __LINE__) = register_funcs
#define register_func(func) reinterpret_cast<uintptr_t> (func), #func

#endif
