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


io<int>
lwt::open (char const *pathname)
{
  int fd = ::open (pathname, 0);
  assert (fd >= 0);
  return success (fd);
}
