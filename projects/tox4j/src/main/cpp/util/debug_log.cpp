#include "util/debug_log.h"

#include <tox/core.h>

#include <cctype>
#include <cstdlib>
#include <iostream>
#include <map>
#include <mutex>

#include <jni.h>

#undef register_func
#define register_func(func) std::make_pair (reinterpret_cast<uintptr_t> (func), #func)

static std::map<uintptr_t, std::string const> &
func_names ()
{
  static std::map<uintptr_t, std::string const> func_names;
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
print_func (protolog::JniLogEntry &log_entry, uintptr_t func)
{
  auto &names = func_names ();
  auto found = names.find (func);
  if (found != names.end ())
    log_entry.set_name (found->second);
  else
    log_entry.set_name (std::to_string (func));
}

template<typename Arg>
void
print_arg (protolog::Value &value, Arg arg)
{
  value.set_string (std::to_string (arg));
}

template void print_arg<bool              > (protolog::Value &, bool              );
template void print_arg<  signed char     > (protolog::Value &,   signed char     );
template void print_arg<unsigned char     > (protolog::Value &, unsigned char     );
template void print_arg<  signed short    > (protolog::Value &,   signed short    );
template void print_arg<unsigned short    > (protolog::Value &, unsigned short    );
template void print_arg<  signed int      > (protolog::Value &,   signed int      );
template void print_arg<unsigned int      > (protolog::Value &, unsigned int      );
template void print_arg<  signed long     > (protolog::Value &,   signed long     );
template void print_arg<unsigned long     > (protolog::Value &, unsigned long     );
template void print_arg<  signed long long> (protolog::Value &,   signed long long);
template void print_arg<unsigned long long> (protolog::Value &, unsigned long long);

template<>
void
print_arg<char const *> (protolog::Value &value, char const *data)
{
  if (data != nullptr)
    value.set_string (data);
  else
    value.set_string ("<null>");
}

template<>
void
print_arg<uint8_t *> (protolog::Value &value, uint8_t *data)
{
  if (data != nullptr)
    value.set_string ("in data");
  else
    value.set_string ("<null>");
}

template<>
void
print_arg<uint8_t const *> (protolog::Value &value, uint8_t const *data)
{
  if (data != nullptr)
    value.set_string ("out data");
  else
    value.set_string ("<null>");
}

void
print_arg (protolog::Value &value, unsigned char const *data, std::size_t length)
{
  if (data != nullptr)
    value.set_string ("out data[" + std::to_string (length) + "]");
  else
    value.set_string ("<null>");
}

template<>
void
print_arg<JNIEnv *> (protolog::Value &value, JNIEnv *data)
{
  if (data != nullptr)
    value.set_string ("<JNIEnv>");
  else
    value.set_string ("<null>");
}

template<>
void
print_arg<jbyteArray> (protolog::Value &value, jbyteArray data)
{
  if (data != nullptr)
    value.set_string ("<jbyteArray>");
  else
    value.set_string ("<null>");
}

template<>
void
print_arg<jintArray> (protolog::Value &value, jintArray data)
{
  if (data != nullptr)
    value.set_string ("<jintArray>");
  else
    value.set_string ("<null>");
}


JniLog jni_log;


struct JniLog::data
{
  int max_size = 100;
  bool enabled = true;
  std::mutex mutex;
  protolog::JniLog log;
};


JniLog::JniLog ()
  : self (std::make_unique<data> ())
{
}

JniLog::~JniLog ()
{
}


JniLog::Entry
JniLog::new_entry ()
{
  if (!self->enabled)
    return { };

  std::unique_lock<std::mutex> lock (self->mutex);
  if (self->log.entries_size () >= self->max_size)
    return nullptr;
  return JniLog::Entry (self->log.add_entries (), std::move (lock));
}

std::vector<char>
JniLog::clear ()
{
  std::lock_guard<std::mutex> lock (self->mutex);
  std::vector<char> buffer (self->log.ByteSize ());
  self->log.SerializeToArray (buffer.data (), buffer.size ());
  self->log.Clear ();

  return buffer;
}

bool
JniLog::empty () const
{
  std::lock_guard<std::mutex> lock (self->mutex);
  return self->log.entries_size () == 0;
}

void
JniLog::enabled (bool enabled)
{
  std::lock_guard<std::mutex> lock (self->mutex);
  self->enabled = enabled;
}

void
JniLog::max_size (int max_size)
{
  std::lock_guard<std::mutex> lock (self->mutex);
  self->max_size = max_size;
}
