#pragma once

#include "lwt/io.h"


namespace lwt
{


/******************************************************************************
 * UNIX I/O
 *****************************************************************************/


struct SystemError
{
  friend void type_name (std::string &name, SystemError const &)
  { name += "SystemError"; }

  explicit SystemError (int error)
    : code (error)
  { }

  int const code;
};

template<typename ...Success>
using io = basic_io<SystemError, Success...>;

template<typename ...Success>
using io_success = states::success_t<Success...>;

using io_failure = states::failure_t<SystemError>;

template<typename Callback>
using io_waiting = states::waiting_t<SystemError, Callback>;



/******************************************************************************
 * shared_fd
 *****************************************************************************/


struct file_descriptor
{
  file_descriptor (file_descriptor const &rhs) = delete;

  explicit file_descriptor (int fd);
  ~file_descriptor ();

  unsigned const fd;

private:
  unsigned refcount = 0;

  friend void intrusive_ptr_add_ref (file_descriptor *p)
  { ++p->refcount; }

  friend void intrusive_ptr_release (file_descriptor *p)
  { if (!--p->refcount) delete p; }
};


typedef boost::intrusive_ptr<file_descriptor> shared_fd;


static inline void type_name (std::string &name, shared_fd const &)
{ name += "shared_fd"; }


static inline std::ostream &
operator << (std::ostream &os, shared_fd const &fd)
{
  return os << fd->fd;
}


/******************************************************************************
 * I/O functions
 *****************************************************************************/


void run (io<> program);

io<shared_fd> open (char const *pathname);
io<std::vector<uint8_t>> read (shared_fd fd, std::size_t count);


}
