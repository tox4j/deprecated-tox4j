#include "event_loop.h"
#include "logging.h"

#include <ev.h>

#include <fcntl.h>
#include <sys/stat.h>
#include <sys/types.h>


using namespace lwt;


/******************************************************************************
 * event_loop
 *****************************************************************************/


struct io_waiting_ref
{
  io_waiting_ref &operator = (io_waiting_ref const &) = delete;

  io_waiting_ref (io_waiting_ref &&rhs)
    : events (rhs.events)
    , io_ (std::move (rhs.io_))
    , fd_ (std::move (rhs.fd_))
  {
    // XXX: only with working move constructor for intrusive_ptr.
    //LOG_ASSERT (rhs.fd_ == nullptr);
    rhs.fd_ = nullptr;
  }

  io_waiting_ref (int events, basic_io_base io, shared_fd fd)
    : events (events)
    , io_ (io)
    , fd_ (fd)
  { }

  ~io_waiting_ref ()
  {
    if (fd_)
      io_.cancel ();
  }

  void notify (int fd)
  {
    SCOPE;
    LOG_ASSERT (fd_ != nullptr);
    LOG_ASSERT (static_cast<unsigned> (fd) == fd_->fd);

    io_.notify (make_ptr<io_success<shared_fd>> (std::move (fd_)));
    // XXX: only with working move constructor for intrusive_ptr.
    //LOG_ASSERT (fd_ == nullptr);
    fd_ = nullptr;
  }

  int events;

//private:
  basic_io_base io_;
  shared_fd fd_;
};


struct print_ev_events
{
  explicit print_ev_events (int events)
    : events_ (events)
  { }

  friend std::ostream &operator << (std::ostream &os, print_ev_events e)
  {
    switch (e.events_)
      {
      case EV_READ:
        return os << "EV_READ";
      case EV_WRITE:
        return os << "EV_WRITE";
      case EV_READ | EV_WRITE:
        return os << "EV_READ | EV_WRITE";
      default:
        return os << "<unknown events>";
      }
  }

private:
  int events_;
};


struct event_loop
{
  struct data_type
  {
    struct ev_loop *const raw_loop = ev_loop_new (EVFLAG_AUTO);
    std::vector<ev_io> io_watchers;
    std::vector<optional<io_waiting_ref>> io_waiting;
  };

  event_loop ()
    : data (new data_type)
  {
    SCOPE;
    LOG (INFO) << "Creating event loop";
  }

  ~event_loop ()
  {
    ev_loop_destroy (data->raw_loop);
  }


  static void io_callback (struct ev_loop *loop, ev_io *w, int events)
  {
    SCOPE;
    (void) loop;
    data_type *data = static_cast<data_type *> (w->data);

    // This fd was never waited on, before.
    LOG_ASSERT (data->io_waiting.size () > static_cast<std::size_t> (w->fd));

    optional<io_waiting_ref> waiting = std::move (data->io_waiting[w->fd]);
    //LOG_ASSERT (!data->io_waiting[w->fd]);
    if (waiting && waiting->events & events)
      {
        assert (!data->io_waiting[w->fd]);
        LOG (INFO) << "Received I/O event on " << w->fd << " for "
                   << print_ev_events (events);
        waiting->notify (w->fd);
      }

    // XXX: IO.ReadMultiplex test fails with these lines.
    //ev_io &watcher = data->io_watchers[w->fd];
    //ev_io_stop  (data->raw_loop, &watcher);
  }

  void add_io (int fd)
  {
    SCOPE;
    LOG (INFO) << "Adding I/O watcher for fd " << fd;

    if (data->io_watchers.size () <= static_cast<std::size_t> (fd))
      data->io_watchers.resize (fd + 1);
    ev_io &io = data->io_watchers[fd];
    io.data = data.get ();
    ev_set_cb (&io, io_callback);
  }

  void remove_io (int fd)
  {
    SCOPE;
    LOG (INFO) << "Removing I/O watcher for fd " << fd;

    LOG_ASSERT (data->io_watchers.size () > static_cast<std::size_t> (fd));
    ev_io_stop (data->raw_loop, &data->io_watchers[fd]);
    data->io_watchers[fd].data = nullptr;

    // Remove waiting IOs, instantly setting it to an error state.
    if (data->io_waiting.size () > static_cast<std::size_t> (fd))
      data->io_waiting[fd] = nullopt_t ();
  }


  template<typename Callback>
  typename std::result_of<Callback (shared_fd)>::type
  wait_io (shared_fd fd, int events, Callback cb)
  {
    SCOPE;
    typedef typename std::result_of<Callback (shared_fd)>::type result_type;

    LOG_ASSERT (data->io_watchers.size () > fd->fd);
    result_type io = make_ptr<io_waiting<Callback>> (cb);

    if (data->io_waiting.size () <= fd->fd)
      data->io_waiting.resize (fd->fd + 1);

    if (data->io_waiting[fd->fd])
      LOG (FATAL) << "Attempted to wait on the same fd (" << fd << ") twice at the same time";

    data->io_waiting[fd->fd].emplace (events, io, fd);

    ev_io &watcher = data->io_watchers[fd->fd];
    ev_io_stop  (data->raw_loop, &watcher);
    ev_io_set (&watcher, fd->fd, events);
    ev_io_start (data->raw_loop, &watcher);

    return std::move (io);
  }


  void run (io<> program)
  {
    SCOPE;
    (void) program;
    while (program.state () == io_state::waiting)
      ev_run (data->raw_loop);
    switch (program.state ())
      {
      case io_state::success:
        LOG (INFO) << "Program terminated with success";
        break;
      case io_state::failure:
        LOG (INFO) << "Program terminated with failure: "
                   << type_cast<io_failure &> (*program.get ()).data.match (
                        [](SystemError const &err) {
                          return strerror (err.code);
                        },
                        [](basic_io_state::cancelled const &) {
                          return "cancelled";
                        }
                      );
        break;
      case io_state::waiting:
        LOG (FATAL) << "Program terminated in waiting state";
        break;
      }

    for (optional<io_waiting_ref> const &waiting : data->io_waiting)
      if (waiting)
        LOG (FATAL) << "Still waiting for " << waiting->fd_;

    for (ev_io const &watcher : data->io_watchers)
      if (watcher.data)
        LOG (FATAL) << "Still have a watcher for " << watcher.fd;
  }

private:
  // Not copyable, moveable or assignable.
  std::unique_ptr<data_type> const data;
};


/******************************************************************************
 * I/O functions
 *****************************************************************************/

static thread_local event_loop default_loop;


void
lwt::run (io<> program)
{
  default_loop.run (program);
}


file_descriptor::file_descriptor (int fd)
  : fd (fd)
{
  LOG_ASSERT (fd >= 0);
  default_loop.add_io (fd);
}

file_descriptor::~file_descriptor ()
{
  default_loop.remove_io (fd);

  if (::close (fd) < 0)
    LOG (FATAL) << "Failed to close fd " << fd << ": " << strerror (errno);
}


io<shared_fd>
lwt::open (char const *pathname)
{
  int fd = ::open (pathname, 0);
  if (fd < 0)
    return failure (SystemError (errno));

  return success (make_ptr<file_descriptor> (fd));
}


io<std::vector<uint8_t>>
lwt::read (shared_fd fd, std::size_t count)
{
  SCOPE;
  LOG (INFO) << "Registering I/O wait for read() on fd " << fd;
  return default_loop.wait_io (fd, EV_READ,
    [count] (shared_fd fd) -> io<std::vector<uint8_t>>
    {
      SCOPE;
      LOG (INFO) << "read() became unblocked; reading " << count << " bytes";

      std::vector<uint8_t> buffer (count);
      int result = ::read (fd->fd, buffer.data (), count);
      if (result < 0)
        return failure (SystemError (errno));
      LOG_ASSERT (static_cast<std::size_t> (result) <= count);
      buffer.resize (result);
      return success (std::move (buffer));
    });
}
