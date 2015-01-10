#include "io.h"
#include "logging.h"

#include "optional.h"
#include "variant.h"

#include <boost/intrusive_ptr.hpp>

#include <memory>
#include <vector>

#include <ev.h>

#include <fcntl.h>
#include <sys/stat.h>
#include <sys/types.h>


namespace lwt
{

static std::size_t object_count;


template<typename ...T>
static std::string
type_name ()
{
  char const *func = strchr (__PRETTY_FUNCTION__, '=') + 2;
  return std::string (func, func + strlen (func) - 1);
}


template<typename ...T>
__attribute__ ((__noreturn__))
void fail ()
{
  LOG (INFO) << type_name<T...> ();
  abort ();
}


template<typename T, typename ...Args>
static boost::intrusive_ptr<T>
make_ptr (Args &&...args)
{
  return boost::intrusive_ptr<T> (new T (std::forward<Args> (args)...));
}


/******************************************************************************
 * basic_io
 *****************************************************************************/

enum class io_state
{
  success,
  failure,
  waiting,
  blocked,
};

struct basic_io_state
{
  std::size_t const id;

  typedef boost::intrusive_ptr<basic_io_state> pointer;

  basic_io_state (basic_io_state const &) = delete;
  basic_io_state &operator = (basic_io_state const &) = delete;

  basic_io_state ()
    : id (object_count++)
    , refcount (0)
  { }

  virtual ~basic_io_state ()
  { }

  friend void intrusive_ptr_add_ref (basic_io_state *p)
  { ++p->refcount; }

  friend void intrusive_ptr_release (basic_io_state *p)
  { if (!--p->refcount) delete p; }

  virtual io_state state () const = 0;

  virtual pointer process (int fd) = 0;
  virtual pointer cancel () = 0;
  virtual pointer notify (pointer success) = 0;

  struct cancelled { };

  std::size_t refcount;

  // Actions that are blocked by this IO.
  std::vector<pointer> blocked;
};


namespace states
{
  template<typename ...Success>
  struct success_t;

  template<typename Failure>
  struct failure_t;

  template<typename Failure, typename Callback, typename Success = typename std::result_of<Callback (int)>::type>
  struct waiting_t;

  template<typename ...Callbacks>
  struct blocked_t;
}


struct basic_io_base
{
  explicit basic_io_base (basic_io_state::pointer io)
    : io (io)
  { LOG_ASSERT (io != nullptr); }

  void process (int fd)
  {
    LOG (INFO) << "[" << io->id << "] Processing event on " << fd;
    io = io->process (fd);
    notify ();
  }

  void cancel ()
  {
    LOG (INFO) << "[" << io->id << "] Cancelled";
    io = io->cancel ();
    notify ();
  }

  basic_io_state::pointer io;

private:
  void notify ()
  {
    while (!this->io->blocked.empty ())
      {
        LOG (INFO) << "[" << io->id << "] Notifying " << this->io->blocked.size () << " blocked IOs";
        auto &io = this->io->blocked.back ();
        io = io->notify (this->io);
        this->io->blocked.pop_back ();
      }
  }
};


template<typename Failure, typename ...Success>
struct basic_io
  : basic_io_base
{
  using success_type = states::success_t<Success...>;
  using failure_type = states::failure_t<Failure>;

  template<typename Callback>
  using waiting_type = states::waiting_t<Failure, Callback>;

  template<typename ...Callbacks>
  using blocked_type = states::blocked_t<Callbacks...>;


  basic_io (boost::intrusive_ptr<success_type> p)
    : basic_io_base (p)
  { check (); }

  basic_io (boost::intrusive_ptr<failure_type> p)
    : basic_io_base (p)
  { check (); }

  template<typename Callback>
  basic_io (boost::intrusive_ptr<waiting_type<Callback>> p)
    : basic_io_base (p)
  { check (); }

  template<typename ...Callbacks>
  basic_io (boost::intrusive_ptr<blocked_type<Callbacks...>> p)
    : basic_io_base (p)
  { check (); }


  template<typename BindF>
  typename std::result_of<BindF (Success...)>::type
  operator ->* (BindF func);

  template<typename BindF>
  typename std::result_of<BindF (Success...)>::type
  operator & (BindF func);

private:
  static void check ()
  {
    static_assert (sizeof (basic_io) == sizeof (basic_io_base),
                   "No additional members must be defined in basic_io.");
  }
};


template<typename ...Success>
struct states::success_t
  : basic_io_state
{
  io_state state () const final { return io_state::success; }

  explicit success_t (Success ...values)
    : data_ (values...)
  {
    LOG (INFO) << "[" << this->id << "] New success_t";
  }

  ~success_t ()
  {
    LOG (INFO) << "[" << this->id << "] Deleting success_t";
  }

  pointer process (int fd) final
  {
    LOG (FATAL) << "Processing event in success value: " << fd;
    return this;
  }

  pointer cancel  () final
  {
    LOG (FATAL) << "Attempted to cancel a success value";
    return this;
  }

  pointer notify (pointer success) final
  {
    LOG (FATAL) << "Notifying success value";
    return this;
  }

  template<typename BindF>
  typename std::result_of<BindF (Success...)>::type
  operator ->* (BindF func)
  {
    return apply (make_seq<sizeof... (Success)> (), func, data_);
  }

private:
  template<std::size_t ...S, typename BindF>
  static typename std::result_of<BindF (Success...)>::type
  apply (seq<S...>, BindF func, std::tuple<Success...> const &data)
  {
    return func (std::get<S> (data)...);
  }

  std::tuple<Success...> data_;
};


template<typename Failure>
struct states::failure_t
  : basic_io_state
{
  io_state state () const final { return io_state::failure; }

  explicit failure_t (Failure failure)
    : data_ (failure)
  {
    LOG (INFO) << "[" << this->id << "] New failure_t";
  }

  ~failure_t ()
  {
    LOG (INFO) << "[" << this->id << "] Deleting failure_t";
  }

  explicit failure_t (cancelled failure)
    : data_ (failure)
  { }

  pointer process (int fd) final
  {
    LOG (FATAL) << "Processing event in failure value: " << fd;
    return this;
  }

  pointer cancel  () final
  {
    LOG (FATAL) << "Attempted to cancel a failure value";
    return this;
  }

  pointer notify (pointer success) final
  {
    LOG (FATAL) << "Notifying failure value";
    return this;
  }

private:
  variant<Failure, cancelled> data_;
};


struct basic_io_blocked_state
  : basic_io_state
{
  io_state state () const final { return io_state::blocked; }
};


template<typename ...Callbacks>
struct states::blocked_t
  : basic_io_blocked_state
{
  explicit blocked_t (Callbacks ...callbacks)
    : data_ (callbacks...)
  {
    LOG (INFO) << "[" << this->id << "] New blocked_t";
  }

  ~blocked_t ()
  {
    LOG (INFO) << "[" << this->id << "] Deleting blocked_t";
  }

  pointer process (int fd) final
  {
    LOG (FATAL) << "Processing event in blocked state: " << fd;
    return this;
  }

  pointer cancel  () final
  {
    LOG (FATAL) << "Attempted to cancel a blocked state";
    return this;
  }

  pointer notify (pointer success) final
  {
    LOG (INFO) << "[" << this->id << "] blocked_t became unblocked by [" << success->id
               << "]; calling " << sizeof... (Callbacks) << " callback(s)";
    LOG_ASSERT (success->state () == io_state::success);
    std::vector<pointer> results;
    invoke_all (make_seq<sizeof... (Callbacks)> (), results, success);
    for (pointer result : results)
      if (result->state () == io_state::failure)
        {
          LOG (INFO) << "[" << this->id << "] One callback returned failure: [" << result->id << "]";
          return result;
        }
    LOG (INFO) << "[" << this->id << "] All callbacks were successful; aggregating results";
    // TODO: aggregation
    return results.back ();
  }

private:
  template<std::size_t ...S>
  void invoke_all (seq<S...>, std::vector<pointer> &results, pointer success)
  {
    invoke_all (results, success, std::get<S> (data_)...);
  }

  static void invoke_all (std::vector<pointer> &results, pointer success)
  { }

  template<typename Head, typename ...Tail>
  static void invoke_all (std::vector<pointer> &results, pointer success, Head head, Tail ...tail)
  {
    results.push_back (invoke_one<Head, decltype (&Head::operator ()), &Head::operator ()>::invoke (success, head));
    invoke_all (results, success, tail...);
  }

  template<typename T, typename M, M Call>
  struct invoke_one;

  template<typename T, typename Result, typename ...Args, Result (T::*Call) (Args...)>
  struct invoke_one<T, Result (T::*) (Args...), Call>
  {
    static pointer invoke (pointer success, T func)
    {
      return static_cast<success_t<Args...> &> (*success) ->* func;
    }
  };

  std::tuple<Callbacks...> data_;
};


template<typename Failure, typename ...Success>
struct basic_io_waiting_state
  : basic_io_state
{
  io_state state () const final { return io_state::waiting; }

  template<typename BindF>
  typename std::result_of<BindF (Success...)>::type
  operator ->* (BindF func);
};


template<typename Failure, typename Callback, typename ...Success>
struct states::waiting_t<Failure, Callback, basic_io<Failure, Success...>>
  : basic_io_waiting_state<Failure, Success...>
{
  typedef typename waiting_t::pointer pointer;
  typedef typename waiting_t::cancelled cancelled;

  typedef typename std::result_of<Callback (int)>::type io_type;

  explicit waiting_t (Callback callback)
    : data_ (callback)
  {
    LOG (INFO) << "[" << this->id << "] New waiting_t";
  }

  ~waiting_t ()
  {
    LOG (INFO) << "[" << this->id << "] Deleting waiting_t";
  }

  pointer process (int fd) final
  {
    LOG (INFO) << "[" << this->id << "] waiting_t became unblocked for fd " << fd;
    auto io = data_ (fd).io;
    io->blocked.insert (io->blocked.end (), this->blocked.begin (), this->blocked.end ());
    return io;
  }

  pointer cancel  () final
  {
    return pointer (make_ptr<states::failure_t<Failure>> (cancelled ()));
  }

  pointer notify (pointer success) final
  {
    LOG (FATAL) << "Attempted to notify waiting I/O";
    return this;
  }

private:
  Callback data_;
};


template<typename Failure, typename ...Success>
template<typename BindF>
typename std::result_of<BindF (Success...)>::type
basic_io<Failure, Success...>::operator ->* (BindF func)
{
  typedef typename std::result_of<BindF (Success...)>::type result_type;

  switch (io->state ())
    {
    case io_state::success:
      return static_cast<states::success_t<Success...> &> (*io)
        ->* func;
    case io_state::failure:
      return result_type (&static_cast<states::failure_t<Failure> &> (*io));
    case io_state::waiting:
      {
        return static_cast<basic_io_waiting_state<Failure, Success...> &> (*io)
          ->* func;
      }
    case io_state::blocked:
      {
        //auto &waiting = static_cast<basic_io_waiting_state<Failure, Success...> &> (*io);

        auto on_ready = [=] (Success ...values) mutable -> basic_io_state::pointer {
          return func (values...).io;
        };

        auto blocked = make_ptr<blocked_type<decltype (on_ready)>> (on_ready);

        LOG (INFO) << "[" << this->io->id << "] Adding blocked IO: [" << blocked->id << "]";
        LOG (INFO) << "[" << this->io->id << "] " << type_name<Success...> ();
        this->io->blocked.push_back (blocked);

        return result_type (blocked);
      }
    }
}


template<typename Failure, typename ...Success>
template<typename BindF>
typename std::result_of<BindF (Success...)>::type
basic_io_waiting_state<Failure, Success...>::operator ->* (BindF func)
{
  typedef typename std::result_of<BindF (Success...)>::type result_type;

  auto on_ready = [=] (Success ...values) mutable -> basic_io_state::pointer {
    return func (values...).io;
  };

  auto blocked = make_ptr<typename basic_io<Failure, Success...>::template blocked_type<decltype (on_ready)>> (on_ready);

  LOG (INFO) << "[" << this->id << "] Adding blocked IO: [" << blocked->id << "]";
  LOG (INFO) << "[" << this->id << "] " << type_name<Success...> ();
  this->blocked.push_back (blocked);

  return result_type (blocked);
}


template<typename ...Types>
boost::intrusive_ptr<states::success_t<Types...>>
success (Types ...values)
{
  return make_ptr<states::success_t<Types...>> (values...);
}


template<typename Error>
boost::intrusive_ptr<states::failure_t<Error>>
failure (Error error)
{
  return make_ptr<states::failure_t<Error>> (error);
}



template<typename ...Blocking>
//states::blocked_t<Blocking...>
boost::intrusive_ptr<states::blocked_t<>>
combine (Blocking ...blocking)
{
  //return states::blocked_t<Blocking...> (blocking...);
  return make_ptr<states::blocked_t<>> ();
}


template<typename Func, typename ...Args>
auto
deferred (Func func, Args ...args)
{
  return [=] () mutable { return func (std::move (args)...); };
}


/******************************************************************************
 * UNIX I/O
 *****************************************************************************/


struct SystemError
{
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

template<typename ...Callbacks>
using io_blocked = states::blocked_t<Callbacks...>;



/******************************************************************************
 * event_loop
 *****************************************************************************/


struct io_waiting_ref
{
  io_waiting_ref (io_waiting_ref const &) = delete;
  io_waiting_ref &operator = (io_waiting_ref const &) = delete;

  io_waiting_ref (int events, basic_io_state::pointer io)
    : events (events)
    , io_ (io)
  { }

  ~io_waiting_ref ()
  {
    if (!invoked)
      io_.cancel ();
  }

  void invoke (int fd)
  {
    io_.process (fd);
    invoked = true;
  }

  int events;

private:
  basic_io_base io_;
  bool invoked = false;
};


struct event_loop
{
  std::size_t const id = object_count++;

  struct data_type
  {
    struct ev_loop *raw_loop = ev_loop_new (EVFLAG_AUTO);
    std::vector<ev_io> io_watchers;
    std::vector<optional<io_waiting_ref>> io_waiting;
  };

  event_loop ()
    : data (new data_type)
  {
    LOG (INFO) << "[" << id << "] Creating event loop";
  }

  ~event_loop ()
  {
    ev_loop_destroy (data->raw_loop);
  }


  static void io_callback (struct ev_loop *loop, ev_io *w, int events)
  {
    data_type *data = static_cast<data_type *> (w->data);
    //LOG (INFO) << "[" << id << "] Received I/O event on " << w->fd << " for " << events;

    if (data->io_waiting.size () <= static_cast<std::size_t> (w->fd))
      // This fd was never waited on, before.
      return;

    optional<io_waiting_ref> &waiting = data->io_waiting[w->fd];
    if (waiting && waiting->events & events)
      {
        waiting->invoke (w->fd);
        data->io_waiting[w->fd] = nullopt_t ();
      }
    ev_io_stop (data->raw_loop, &data->io_watchers[w->fd]);
  }

  void add_io (int fd)
  {
    LOG (INFO) << "[" << id << "] Adding I/O watcher for fd " << fd;

    data->io_watchers.resize (fd + 1);
    ev_io &io = data->io_watchers[fd];
    io.data = data.get ();
    ev_set_cb (&io, io_callback);
    ev_io_set (&io, fd, EV_READ | EV_WRITE);
  }

  void remove_io (int fd)
  {
    LOG (INFO) << "[" << id << "] Removing I/O watcher for fd " << fd;

    LOG_ASSERT (data->io_watchers.size () > static_cast<std::size_t> (fd));
    ev_io_stop (data->raw_loop, &data->io_watchers[fd]);

    // Remove waiting IOs, instantly setting it to an error state.
    LOG_ASSERT (data->io_waiting.size () > static_cast<std::size_t> (fd));
    data->io_waiting[fd] = nullopt_t ();
  }


  template<typename Callback>
  typename std::result_of<Callback (int)>::type
  wait_io (int fd, int events, Callback cb)
  {
    auto io = make_ptr<io_waiting<Callback>> (cb);

    if (data->io_waiting.size () <= static_cast<std::size_t> (fd))
      data->io_waiting.resize (fd + 1);

    if (data->io_waiting[fd])
      LOG (FATAL) << "Attempted to wait on the same fd twice";

    data->io_waiting[fd].emplace (events, io);
    ev_io_start (data->raw_loop, &data->io_watchers[fd]);

    return io;
  }


  void run (io<> program)
  {
    ev_run (data->raw_loop);
  }

private:
  // Not copyable, moveable or assignable.
  std::unique_ptr<data_type> const data;
};


/******************************************************************************
 * I/O functions
 *****************************************************************************/


static thread_local event_loop default_loop;


io<int>
open (char const *pathname)
{
  int fd = ::open (pathname, O_NONBLOCK);
  if (fd == -1)
    return failure (SystemError (errno));

  default_loop.add_io (fd);

  return success (fd);
}


io<>
close (int fd)
{
  if (::close (fd) == -1)
    return failure (SystemError (errno));

  default_loop.remove_io (fd);

  return success ();
}


io<std::vector<uint8_t>>
read (int fd, std::size_t count, std::vector<uint8_t> &&buffer = std::vector<uint8_t> ())
{
  return default_loop.wait_io (fd, EV_READ,
    [count, buffer = std::move (buffer)] (int fd) mutable
      -> io<std::vector<uint8_t>>
    {
      if (buffer.size () < count)
        buffer.resize (count);
      int result = ::read (fd, buffer.data (), count);
      if (result == -1)
        return failure (SystemError (errno));
      LOG_ASSERT (static_cast<std::size_t> (result) <= count);
      buffer.resize (result);
      return success (buffer);
    });
}


void
io_main ()
{
  typedef std::vector<uint8_t> byte_vec;

  io<> program = open ("/dev/random")
    ->* [] (int fd) -> io<> {
      io<byte_vec> waiting_read = read (fd, 10);

      // First waiting operation.
      io<> one = waiting_read
        ->* [=] (byte_vec const &buffer1) -> io<> {
          LOG (INFO) << "got buffer 1: " << buffer1.size ();
          return success ();
        }

        ->* deferred (read, fd, 10, byte_vec ())

        ->* [=] (byte_vec const &buffer2) -> io<> {
          LOG (INFO) << "got buffer 2: " << buffer2.size ();
          return close (fd);
        };


      // Second waiting operation.
      io<> two = waiting_read
        ->* [=] (byte_vec const &buffer1) -> io<> {
          LOG (INFO) << "got buffer 1 again: " << buffer1.size ();
          return success ();
        };

      //return combine (one, two);
      return one;
    };

  default_loop.run (program);
}


}
