#include "lwt/io.h"
#include "lwt/logging.h"

using namespace lwt;


std::size_t basic_io_state::object_count;
std::size_t basic_io_base::object_count;


io_state
lwt::operator || (io_state lhs, io_state rhs)
{
  if (lhs == io_state::waiting || rhs == io_state::waiting)
    return io_state::waiting;
  if (lhs == io_state::failure || rhs == io_state::failure)
    return io_state::failure;
  return io_state::success;
}

std::ostream &
lwt::operator << (std::ostream &os, io_state state)
{
  switch (state)
    {
    case io_state::success: return os << "success";
    case io_state::failure: return os << "failure";
    case io_state::waiting: return os << "waiting";
    }
}
