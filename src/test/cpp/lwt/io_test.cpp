#include "lwt/io.h"
#include "lwt/logging.h"

#include "lwt/optional.h"
#include "lwt/variant.h"
#include "lwt/types.h"

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
  std::size_t const tag;
  std::size_t const id;

  typedef boost::intrusive_ptr<basic_io_state> pointer;

  basic_io_state (basic_io_state const &) = delete;
  basic_io_state &operator = (basic_io_state const &) = delete;

  basic_io_state (std::size_t tag)
    : tag (tag)
    , id (object_count++)
    , refcount (0)
  { }

  virtual ~basic_io_state ()
  {
    LOG_ASSERT (blocked.empty ());
  }

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

  basic_io_state::pointer io;
};


template<typename Failure, typename ...Success>
struct basic_io
  : basic_io_base
{
  friend void type_name (std::string &name, basic_io const &)
  {
    name += "basic_io<";
    type_name<Failure, Success...> (name);
    name += ">";
  }

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

private:
  static void check ()
  {
    static_assert (sizeof (basic_io) == sizeof (basic_io_base),
                   "No additional members must be defined in basic_io.");
  }
};


template<bool ...Values>
struct and_type;

template<bool B, bool ...Values>
struct and_type<B, Values...>
{
  static bool const value = B && and_type<Values...>::value;
};

template<>
struct and_type<>
  : std::true_type
{ };


template<typename ...Success>
struct states::success_t
  : basic_io_state
{
  friend void type_name (std::string &name, success_t const &)
  {
    name += "success_t<";
    type_name<Success...> (name);
    name += ">";
  }

  static std::size_t const TAG;

  io_state state () const final { return io_state::success; }

  static_assert (and_type<std::is_same<typename std::remove_reference<Success>::type, Success>::value...>::value,
                 "No references allowed in success_t");

  explicit success_t (Success const &...values)
    : basic_io_state (TAG)
    , data_ (values...)
  {
    LOG (INFO) << "[" << this->id << "] New success_t";
  }

  ~success_t ()
  {
    LOG (INFO) << "[" << this->id << "] Deleting success_t";
  }

  pointer process (int fd) final
  {
    LOG (FATAL) << "[" << this->id << "] Processing event in success value: " << fd;
    return this;
  }

  pointer cancel  () final
  {
    LOG (FATAL) << "[" << this->id << "] Attempted to cancel a success value";
    return this;
  }

  pointer notify (pointer success) final
  {
    LOG (FATAL) << "[" << this->id << "] Notifying success value with [" << success->id << "]";
    return this;
  }

  template<typename BindF>
  typename std::result_of<BindF (Success...)>::type
  operator ->* (BindF func)
  {
    return apply (make_seq<sizeof... (Success)> (), func);
  }

private:
  template<std::size_t ...S, typename BindF>
  typename std::result_of<BindF (Success...)>::type
  apply (seq<S...>, BindF func)
  {
    return func (std::get<S> (data_)...);
  }

  std::tuple<Success...> data_;
};

template<typename ...Success>
std::size_t const states::success_t<Success...>::TAG = types::make<states::success_t<Success...>> ();


template<typename Failure>
struct states::failure_t
  : basic_io_state
{
  friend void type_name (std::string &name, failure_t const &)
  {
    name += "failure_t<";
    type_name<Failure> (name);
    name += ">";
  }

  static std::size_t const TAG;

  io_state state () const final { return io_state::failure; }

  explicit failure_t (Failure failure)
    : basic_io_state (TAG)
    , data_ (failure)
  {
    LOG (INFO) << "[" << this->id << "] New failure_t";
  }

  ~failure_t ()
  {
    LOG (INFO) << "[" << this->id << "] Deleting failure_t";
  }

  explicit failure_t (cancelled failure)
    : basic_io_state (TAG)
    , data_ (failure)
  { }

  pointer process (int fd) final
  {
    LOG (FATAL) << "[" << this->id << "] Processing event in failure value: " << fd;
    return this;
  }

  pointer cancel  () final
  {
    LOG (FATAL) << "[" << this->id << "] Attempted to cancel a failure value";
    return this;
  }

  pointer notify (pointer success) final
  {
    LOG (FATAL) << "[" << this->id << "] Notifying failure value with [" << success->id << "]";
    return this;
  }

private:
  variant<Failure, cancelled> data_;
};

template<typename Failure>
std::size_t const states::failure_t<Failure>::TAG = types::make<states::failure_t<Failure>> ();


struct basic_io_blocked_state
  : basic_io_state
{
  using basic_io_state::basic_io_state;

  io_state state () const final { return io_state::blocked; }
};


template<typename ...Callbacks>
struct states::blocked_t
  : basic_io_blocked_state
{
  friend void type_name (std::string &name, blocked_t const &)
  {
    name += "blocked_t<";
    type_name<Callbacks...> (name);
    name += ">";
  }

  static std::size_t const TAG;

  explicit blocked_t (Callbacks ...callbacks)
    : basic_io_blocked_state (TAG)
    , data_ (callbacks...)
  {
    LOG (INFO) << "[" << this->id << "] New blocked_t";
  }

  ~blocked_t ()
  {
    LOG (INFO) << "[" << this->id << "] Deleting blocked_t";
  }

  pointer process (int fd) final
  {
    LOG (FATAL) << "[" << this->id << "] Processing event in blocked state: " << fd;
    return this;
  }

  pointer cancel  () final
  {
    LOG (FATAL) << "[" << this->id << "] Attempted to cancel a blocked state";
    return this;
  }

  pointer notify (pointer success) final
  {
    LOG (INFO) << "[" << this->id << "] blocked_t became unblocked by [" << success->id
               << "]; calling " << sizeof... (Callbacks) << " callback(s)";
    LOG_ASSERT (success->state () == io_state::success);
    std::vector<basic_io_base> results;
    invoke_all (make_seq<sizeof... (Callbacks)> (), results, success);
    for (basic_io_base result : results)
      {
        if (result.io->state () == io_state::failure)
          {
            LOG (INFO) << "[" << this->id << "] One callback returned failure: [" << result.io->id << "]";
            return result.io;
          }
        else if (result.io->state () == io_state::success)
          result.notify ();
      }
    LOG (INFO) << "[" << this->id << "] All callbacks were successful; aggregating results";
    // TODO: aggregation
    return results.back ().io;
  }

private:
  template<std::size_t ...S>
  void invoke_all (seq<S...>, std::vector<basic_io_base> &results, pointer success)
  {
    invoke_all (results, success, std::get<S> (data_)...);
  }

  static void invoke_all (std::vector<basic_io_base> &/*results*/, pointer /*success*/)
  { }

  template<typename Head, typename ...Tail>
  static void invoke_all (std::vector<basic_io_base> &results, pointer success, Head head, Tail ...tail)
  {
    results.push_back (invoke_one (&Head::operator (), success, head));
    invoke_all (results, success, tail...);
  }

  template<typename T, typename Result, typename ...Args>
  static Result invoke_one_helper (pointer success, T func)
  {
    return type_cast<success_t<typename std::decay<Args>::type...> &> (*success) ->* func;
  }

  template<typename T, typename Result, typename ...Args>
  static Result invoke_one (Result (T::*) (Args...), pointer success, T func)
  { return invoke_one_helper<T, Result, Args...> (success, func); }

  template<typename T, typename Result, typename ...Args>
  static Result invoke_one (Result (T::*) (Args...) const, pointer success, T func)
  { return invoke_one_helper<T, Result, Args...> (success, func); }

  std::tuple<Callbacks...> data_;
};

template<typename ...Callbacks>
std::size_t const states::blocked_t<Callbacks...>::TAG = types::make<states::blocked_t<Callbacks...>> ();


template<typename Failure, typename ...Success>
struct basic_io_waiting_state
  : basic_io_state
{
  friend void type_name (std::string &name, basic_io_waiting_state const &)
  {
    name += "waiting_t<";
    type_name<Failure, Success...> (name);
    name += ">";
  }

  static std::size_t const TAG;

  basic_io_waiting_state ()
    : basic_io_state (TAG)
  { }

  io_state state () const final { return io_state::waiting; }

  template<typename BindF>
  typename std::result_of<BindF (Success...)>::type
  operator ->* (BindF func);
};

template<typename Failure, typename ...Success>
std::size_t const basic_io_waiting_state<Failure, Success...>::TAG = types::make<basic_io_waiting_state<Failure, Success...>> ();


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
    this->blocked.clear ();
    return io;
  }

  pointer cancel  () final
  {
    return pointer (make_ptr<states::failure_t<Failure>> (cancelled ()));
  }

  pointer notify (pointer success) final
  {
    LOG (FATAL) << "[" << this->id << "] Attempted to notify waiting I/O with [" << success->id << "]";
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
      return type_cast<states::success_t<Success...> &> (*io)
        ->* func;
    case io_state::failure:
      return result_type (&type_cast<states::failure_t<Failure> &> (*io));
    case io_state::waiting:
      return type_cast<basic_io_waiting_state<Failure, Success...> &> (*io)
        ->* func;
    case io_state::blocked:
      {
        //auto &waiting = type_cast<basic_io_waiting_state<Failure, Success...> &> (*io);

        auto blocked = make_ptr<blocked_type<BindF>> (func);

        LOG (INFO) << "[" << this->io->id << "] Adding blocked IO: [" << blocked->id << "]";
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

  auto blocked = make_ptr<typename basic_io<Failure, Success...>::template blocked_type<BindF>> (func);

  LOG (INFO) << "[" << this->id << "] Adding blocked IO: [" << blocked->id << "]";
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
combine (Blocking .../*blocking*/)
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
    (void) loop;
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
    (void) program;
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
read (int fd, std::size_t count)
{
  return default_loop.wait_io (fd, EV_READ,
    [count] (int fd) mutable
      -> io<std::vector<uint8_t>>
    {
      std::vector<uint8_t> buffer (count);
      int result = ::read (fd, buffer.data (), count);
      if (result == -1)
        return failure (SystemError (errno));
      LOG_ASSERT (static_cast<std::size_t> (result) <= count);
      buffer.resize (result);
      return success (buffer);
    });
}


}


#include "lwt/io.h"
#include "lwt/logging.h"
#include <gtest/gtest.h>


namespace lwt
{


TEST (IO, Read) {
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

        ->* deferred (read, fd, 10)

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
