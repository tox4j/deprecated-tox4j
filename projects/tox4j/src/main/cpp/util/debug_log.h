#ifndef DEBUG_LOG_H
#define DEBUG_LOG_H

#include "util/pp_attributes.h"
#include "util/pp_cat.h"
#include "util/unused.h"
#include "util/wrap_void.h"

#include "ProtoLog.pb.h"

#include <chrono>
#include <memory>
#include <mutex>

namespace protolog = im::tox::tox4j::impl::jni::proto;


/****************************************************************************
 *
 * print_arg: Writes a single argument or return value to a Value.
 *
 ****************************************************************************/


template<typename Arg>
void print_arg (protolog::Value &value, Arg arg);

template<>
void print_arg (protolog::Value &value, char const *arg);

void print_arg (protolog::Value &value, uint8_t const *data, std::size_t length);

template<typename Arg, typename ArgDeleter>
void
print_arg (protolog::Value &value, std::unique_ptr<Arg, ArgDeleter> const &arg)
{
  print_arg (value, arg.get ());
}

template<typename Arg>
void
print_arg (protolog::Value &value, wrapped_value<Arg> const &arg)
{
  print_arg (value, arg.value);
}

static inline void
print_arg (protolog::Value &value, wrapped_value<void> const &arg)
{
  unused (value, arg);
}


/****************************************************************************
 *
 * print_member: Adds a name/value pair to an object type Value.
 *
 ****************************************************************************/


template<typename T>
void
print_member (protolog::Value &value, char const *name, T member_value)
{
  auto *member = value.add_object ();
  member->set_name (name);
  print_arg (*member->mutable_value (), member_value);
}

static inline void
print_member (protolog::Value &value, char const *name, uint8_t const *data, std::size_t length)
{
  auto *member = value.add_object ();
  member->set_name (name);
  print_arg (*member->mutable_value (), data, length);
}


/****************************************************************************
 *
 * print_args: Call print_arg on all arguments and adds them to the log entry.
 *
 ****************************************************************************/


static inline void
print_args (protolog::JniLogEntry &log_entry)
{
  unused (log_entry);
}

template<typename Arg0, typename ...Args>
void print_args (protolog::JniLogEntry &log_entry,
                 Arg0 arg0, Args ...args);

template<typename ...Args>
void
print_args (protolog::JniLogEntry &log_entry,
            uint8_t const *data, std::size_t size, Args ...args)
{
  protolog::Value *value = log_entry.add_arguments ();
  print_arg (*value, data, size);
  print_args (log_entry, args...);
}

template<typename Arg0, typename ...Args>
void
print_args (protolog::JniLogEntry &log_entry,
            Arg0 arg0, Args ...args)
{
  protolog::Value *value = log_entry.add_arguments ();
  print_arg (*value, arg0);
  print_args (log_entry, args...);
}


/****************************************************************************
 *
 * print_func: Look up the name of a function and set it in the log entry.
 *
 ****************************************************************************/


void print_func (protolog::JniLogEntry &log_entry, uintptr_t func);


/****************************************************************************
 *
 * JniLog and LogEntry
 *
 ****************************************************************************/


struct JniLog
{
  struct Entry
  {
    Entry (protolog::JniLogEntry *entry = nullptr, std::unique_lock<std::mutex> lock = { })
      : entry (entry)
      , lock (std::move (lock))
    { }

    protolog::JniLogEntry *operator -> () const { return  entry; }
    protolog::JniLogEntry &operator *  () const { return *entry; }
    explicit operator bool () const { return entry; }

  private:
    protolog::JniLogEntry *entry;
    std::unique_lock<std::mutex> lock;
  };

  JniLog ();
  ~JniLog ();

  Entry new_entry ();
  std::vector<char> clear ();
  bool empty () const;
  void enabled (bool enabled);
  void max_size (int max_size);

private:
  struct data;
  std::unique_ptr<data> self;
};

extern JniLog jni_log;


struct LogEntry
{
  template<typename Func, typename ...Args>
  LogEntry (Func func, Args ...args)
  {
    static_assert (
      std::is_function<typename std::remove_pointer<Func>::type>::value,
      "The first argument to LogEntry must be a function or instance number."
    );

    if (entry)
      {
        print_func (*entry, reinterpret_cast<uintptr_t> (func));
        print_args (*entry, args...);
      }
  }

  template<typename Func, typename ...Args>
  LogEntry (int instanceNumber, Func func, Args ...args)
    : LogEntry (func, args...)
  {
    if (entry)
      entry->set_instance_number (instanceNumber);
  }


  template<typename FuncT, typename ...Args>
  auto
  print_result (FuncT func, Args ...args)
  {
    if (entry)
      {
        auto start = std::chrono::system_clock::now();
        auto result = wrap_void (func, args...);
        auto end = std::chrono::system_clock::now();

        entry->set_elapsed_time (
          std::chrono::duration_cast<std::chrono::microseconds> (end - start).count ()
        );

        print_arg (*entry->mutable_result (), result);

        return result.unwrap ();
      }

    return func (args...);
  }

private:
  JniLog::Entry const entry = jni_log.new_entry ();
};


/****************************************************************************
 *
 * register_funcs: Function registry (map of address to name) for logging.
 *
 ****************************************************************************/


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

#define register_funcs static PP_UNUSED bool const PP_CAT (register_funcs_, __LINE__) = register_funcs
#define register_func(func) reinterpret_cast<uintptr_t> (func), #func

#endif
