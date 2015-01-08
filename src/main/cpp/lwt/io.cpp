#include "io.h"

#include <fcntl.h>
#include <sys/stat.h>
#include <sys/types.h>

using namespace lwt;


void
lwt::intrusive_ptr_add_ref (io_base *io)
{
  ++io->refcount;
}

void
lwt::intrusive_ptr_release (io_base *io)
{
  --io->refcount;
}


void
lwt::eval (io<> io)
{
  ptr<io_base> base = io;

  bool done = false;
  while (!done)
    base = base->step (done);
}


io<>
lwt::unit ()
{
  return new io_success<>;
}


io<int>
lwt::open (char const *pathname)
{
  int fd = ::open (pathname, 0);
  assert (fd >= 0);
  return success (fd);
}


io<std::vector<uint8_t>>
lwt::read (int fd, std::size_t count, std::vector<uint8_t> &&buffer)
{
  if (buffer.size () < count)
    buffer.resize (count);
  int result = ::read (fd, buffer.data (), count);
  assert (result >= 0);
  return success (buffer);
}
