#include "lwt/io.h"
#include "lwt/logging.h"

#include "lwt/optional.h"
#include "lwt/variant.h"
#include "lwt/types.h"

#include <boost/intrusive_ptr.hpp>

#include <algorithm>
#include <memory>
#include <vector>

#include <ev.h>

#include <fcntl.h>
#include <sys/stat.h>
#include <sys/types.h>


static std::size_t scope;
struct scope_counter
{
  scope_counter ()
  { scope++; }

  ~scope_counter ()
  { scope--; }

  scope_counter (scope_counter const &rhs) = delete;
};

static std::ostream &
scope_indent (std::ostream &os, int line)
{
  for (std::size_t i = 0; i < scope - (line >= 1000); i++)
    os << ' ';
  return os;
}

#define SCOPE scope_counter const _scope
#undef LOG
#define LOG(KIND) scope_indent (COMPACT_GOOGLE_LOG_##KIND.stream(), __LINE__)


namespace lwt
{


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
};

io_state
operator || (io_state lhs, io_state rhs)
{
  if (lhs == io_state::waiting || rhs == io_state::waiting)
    return io_state::waiting;
  if (lhs == io_state::failure || rhs == io_state::failure)
    return io_state::failure;
  return io_state::success;
}

std::ostream &
operator << (std::ostream &os, io_state state)
{
  switch (state)
    {
    case io_state::success: return os << "success";
    case io_state::failure: return os << "failure";
    case io_state::waiting: return os << "waiting";
    }
}

struct basic_io_base;

struct basic_io_state
{
  static std::size_t object_count;

  std::size_t const tag;
  std::size_t const id = object_count++;

  typedef boost::intrusive_ptr<basic_io_state> pointer;

  std::string type () const { return types::name (tag); }

  basic_io_state (basic_io_state const &) = delete;
  basic_io_state &operator = (basic_io_state const &) = delete;

  basic_io_state (std::size_t tag)
    : tag (tag)
    , refcount (0)
  { }

  virtual ~basic_io_state ()
  { }

  friend void intrusive_ptr_add_ref (basic_io_state *p)
  { ++p->refcount; }

  friend void intrusive_ptr_release (basic_io_state *p)
  { if (!--p->refcount) delete p; }

  virtual io_state state () const = 0;

  virtual basic_io_base notify (basic_io_state::pointer const &result) = 0;
  virtual void notify (std::vector<basic_io_base> &blocked) = 0;
  virtual pointer cancel () = 0;

  struct cancelled { };

  std::size_t refcount;
};

std::size_t basic_io_state::object_count;


namespace states
{
  template<typename ...Success>
  struct success_t;

  template<typename Failure>
  struct failure_t;

  template<typename Failure, typename Callback>
  struct waiting_t;
}


struct basic_io_base;
void dump_ios ();
void register_io (basic_io_base const &io);


struct basic_io_base
{
  static std::size_t object_count;

  explicit basic_io_base (basic_io_state::pointer const &io)
    : state_ (make_ptr<data> (io))
  {
    SCOPE;
    LOG_ASSERT (io != nullptr);
    LOG (INFO) << *this << "Creating io_base in state " << io->state ();
    //register_io (*this);
  }

  basic_io_base &operator = (basic_io_base &&rhs)
  {
    state_->io = std::move (rhs.state_->io);
    state_->blocked = std::move (rhs.state_->blocked);
    rhs.reset ();
    return *this;
  }

  basic_io_base (basic_io_base &&rhs)
    : state_ (rhs.state_)
  { rhs.reset (); }

  basic_io_base (basic_io_base const &rhs)
    : state_ (rhs.state_)
  { }

  void reset ()
  { state_ = nullptr; }

  std::size_t id () const { return state_->io->id; }
  io_state state () const { return state_->io->state (); }
  std::string type () const { return state_->io->type (); }

  bool transition (basic_io_base const &new_state)
  {
    SCOPE;
    if (new_state == *this)
      {
        LOG (INFO) << *this << "Not transitioning";
        return false;
      }

    LOG (INFO) << *this << "Transitioned from "
               << state () << " [" << id () << "] to " << new_state.state ()
               << " [" << new_state.id () << "]";
    state_->io = new_state.state_->io;

    LOG (INFO) << *this << "Moving "
               << new_state.state_->blocked.size ()
               << " blocked states from [" << new_state.state_->id << "/_]";

    while (!new_state.state_->blocked.empty ())
      {
        state_->blocked.push_back (std::move (new_state.state_->blocked.back ()));
        new_state.state_->blocked.pop_back ();
      }
    LOG (INFO) << *this << "Now contains " << state_->blocked.size () << " blocked states";
    //dump_ios ();

    return true;
  }


  void notify (basic_io_state::pointer const &result)
  {
    SCOPE;
    LOG (INFO) << *this << "Received notification from [" << result->id << "]";
    basic_io_base const &new_state = state_->io->notify (result);
    if (transition (new_state))
      {
        state_->io->notify (state_->blocked);
        while (!state_->blocked.empty ())
          {
            new_state.state_->blocked.push_back (std::move (state_->blocked.back ()));
            state_->blocked.pop_back ();
          }
      }
  }

  void cancel ()
  {
    SCOPE;
    LOG (INFO) << *this << "Cancelled";
    //transition (state_->io->cancel ());
    assert (false);
  }

  void print_blocked () const
  {
    for (basic_io_base const &blocked : state_->blocked)
      LOG (INFO) << *this << " is blocking " << blocked;
  }

  basic_io_state::pointer const &get () const
  {
    return state_->io;
  }

  friend std::ostream &operator << (std::ostream &os, basic_io_base const &io)
  { return os << "[" << io.state_->id << "/" << io.id () << "] "; }

  friend bool operator == (basic_io_base const &lhs, basic_io_base const &rhs)
  { return lhs.state_ == rhs.state_; }

  friend bool operator != (basic_io_base const &lhs, basic_io_base const &rhs)
  { return lhs.state_ != rhs.state_; }

protected:
  struct data
  {
    explicit data (basic_io_state::pointer const &io)
      : io (io)
    { }

    ~data ()
    {
      SCOPE;
      if (!blocked.empty ())
        LOG (FATAL) << "[" << id << "] Deleting IO with blocked states";
    }

    friend void intrusive_ptr_add_ref (data *p)
    { ++p->refcount; }

    friend void intrusive_ptr_release (data *p)
    { if (!--p->refcount) delete p; }

    std::size_t const id = object_count++;
    std::size_t refcount = 0;
    basic_io_state::pointer io;
    // Actions that are blocked by this IO.
    std::vector<basic_io_base> blocked;
  };

  boost::intrusive_ptr<data> state_;

  friend struct io_registry;
};

std::size_t basic_io_base::object_count;


static std::ostream &
operator << (std::ostream &os, std::vector<basic_io_base> const &vec)
{
  os << "{";
  for (basic_io_base const &io : vec)
    os << io;
  os << "}";
  return os;
}

static struct io_registry
{
  void dump ()
  {
    SCOPE;
    LOG (INFO) << "Live IOs:";
    for (basic_io_base const &io : ios)
      if (io.state_->refcount > 1)
        LOG (INFO) << "- " << io << io.type () << "; blocked = " << io.state_->blocked;
  }

  std::vector<basic_io_base> ios;
} ios;

void
dump_ios ()
{
  ios.dump ();
}

void
register_io (basic_io_base const &io)
{
  ios.ios.push_back (io);
  dump_ios ();
}


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


  basic_io (boost::intrusive_ptr<success_type> const &p)
    : basic_io_base (p)
  { check (); }

  basic_io (boost::intrusive_ptr<failure_type> const &p)
    : basic_io_base (p)
  { check (); }

  template<typename Callback>
  basic_io (boost::intrusive_ptr<waiting_type<Callback>> const &p)
    : basic_io_base (p)
  { check (); }


  template<typename BindF>
  typename std::result_of<BindF (Success...)>::type
  operator ->* (BindF func) const;


  void add_blocked () { }

  template<typename ...Tail>
  void add_blocked (basic_io head, Tail const &...tail)
  {
    SCOPE;
    LOG (INFO) << "[" << head.id () << "] Adding blocked IO [" << id () << "]";
    head.state_->blocked.push_back (*this);
    return add_blocked (tail...);
  }

private:
  static void check ()
  {
    static_assert (sizeof (basic_io) == sizeof (basic_io_base),
                   "No additional members must be defined in basic_io.");
  }
};


#if 0
template<typename Failure, typename Head, typename ...Tail>
basic_io<Failure, Head, Tail...>
aggregate (std::vector<basic_io<Failure, Head, Tail...>> const &results)
{
  LOG (FATAL) << "Unimplemented aggregation for " << results.size () << " "
              << types::name<basic_io<Failure, Head, Tail...>> ();
}


/**
 * Default aggregation for io<>.
 */
template<typename Failure>
basic_io<Failure>
aggregate (std::vector<basic_io<Failure>> const &results)
{
  for (basic_io<Failure> const &result : results)
    {
      LOG_ASSERT (result.state () == io_state::success);
      LOG_ASSERT (!result.is_blocked ());
    }
  return results.back ();
}
#endif



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

  static_assert (and_type<std::is_same<
                   typename std::remove_reference<Success>::type,
                   Success
                 >::value...>::value,
                 "No references allowed in success_t");

  explicit success_t (Success const &...values)
    : basic_io_state (TAG)
    , data_ (values...)
  {
    SCOPE;
    LOG (INFO) << "[_/" << this->id << "] New success_t";
  }

  ~success_t ()
  {
    SCOPE;
    LOG (INFO) << "[_/" << this->id << "] Deleting success_t";
  }

  basic_io_base notify (basic_io_state::pointer const &result) final
  {
    SCOPE;
    LOG (FATAL) << "[_/" << this->id << "] Processing event in success value: " << result->state ();
  }

  void notify (std::vector<basic_io_base> &blocked) final
  {
    SCOPE;
    LOG (INFO) << "[_/" << this->id << "] Notifying " << blocked.size () << " states of success";
    while (!blocked.empty ())
      {
        basic_io_base io = std::move (blocked.back ());
        blocked.pop_back ();
        io.notify (this);
      }
  }

  pointer cancel () final
  {
    SCOPE;
    LOG (FATAL) << "[_/" << this->id << "] Attempted to cancel a success value";
    return this;
  }

  template<typename BindF>
  typename std::result_of<BindF (Success...)>::type
  operator ->* (BindF func) const
  {
    return apply (make_seq<sizeof... (Success)> (), func);
  }

private:
  template<std::size_t ...S, typename BindF>
  typename std::result_of<BindF (Success...)>::type
  apply (seq<S...>, BindF func) const
  {
    return func (std::get<S> (data_)...);
  }

  std::tuple<Success...> const data_;
};

template<typename ...Success>
std::size_t const states::success_t<Success...>::TAG
    = types::make<states::success_t<Success...>> ();


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
    , data (failure)
  {
    SCOPE;
    LOG (INFO) << "[_/" << this->id << "] New failure_t";
  }

  ~failure_t ()
  {
    SCOPE;
    LOG (INFO) << "[_/" << this->id << "] Deleting failure_t";
  }

  explicit failure_t (cancelled failure)
    : basic_io_state (TAG)
    , data (failure)
  { }

  basic_io_base notify (basic_io_state::pointer const &result) final
  {
    SCOPE;
    LOG (FATAL) << "[_/" << this->id << "] Processing event in failure value: " << result->state ();
  }

  void notify (std::vector<basic_io_base> &blocked) final
  {
    SCOPE;
    LOG (INFO) << "[_/" << this->id << "] Notifying " << blocked.size () << " states of failure";
    while (!blocked.empty ())
      {
        basic_io_base io = std::move (blocked.back ());
        blocked.pop_back ();
        io.notify (this);
      }
  }

  pointer cancel () final
  {
    SCOPE;
    LOG (FATAL) << "[_/" << this->id << "] Attempted to cancel a failure value";
    return this;
  }

  variant<Failure, cancelled> const data;
};

template<typename Failure>
std::size_t const states::failure_t<Failure>::TAG
    = types::make<states::failure_t<Failure>> ();


template<typename Failure, typename Callback>
struct states::waiting_t
  : basic_io_state
{
  typedef typename waiting_t::pointer pointer;
  typedef typename waiting_t::cancelled cancelled;

  friend void type_name (std::string &name, waiting_t const &)
  {
    name += "waiting_t<";
    type_name<Failure, Callback> (name);
    name += ">";
  }

  static std::size_t const TAG;

  io_state state () const final { return io_state::waiting; }

  waiting_t ()
    : basic_io_state (TAG)
  { }

  explicit waiting_t (Callback callback)
    : basic_io_state (TAG)
    , callback_ (callback)
  {
    SCOPE;
    LOG (INFO) << "[_/" << this->id << "] New waiting_t";
  }

  ~waiting_t ()
  {
    SCOPE;
    LOG (INFO) << "[_/" << this->id << "] Deleting waiting_t";
  }

  basic_io_base notify (basic_io_state::pointer const &result) final
  {
    SCOPE;
    LOG (INFO) << "[_/" << this->id << "] waiting_t became unblocked";
    return invoke_one (&Callback::operator (), callback_, result);
  }

  void notify (std::vector<basic_io_base> &blocked) final
  {
    SCOPE;
    LOG (INFO) << "[_/" << this->id << "] Notifying " << blocked.size ()
               << " states in waiting state has no effect";
  }

  pointer cancel () final
  {
    return pointer (make_ptr<states::failure_t<Failure>> (cancelled ()));
  }

private:
  template<typename T, typename IO, typename ...Args>
  static IO invoke_one_helper (T func, basic_io_state::pointer const &result)
  {
    switch (result->state ())
      {
      case io_state::success:
        return type_cast<success_t<typename std::decay<Args>::type...> const &> (*result)
          ->* func;
      case io_state::failure:
        return IO (&type_cast<failure_t<Failure> &> (*result));
      case io_state::waiting:
        assert (false);
      }
  }

  template<typename T, typename IO, typename ...Args>
  static IO invoke_one (IO (T::*) (Args...), T func, basic_io_state::pointer const &result)
  { return invoke_one_helper<T, IO, Args...> (func, result); }

  template<typename T, typename IO, typename ...Args>
  static IO invoke_one (IO (T::*) (Args...) const, T func, basic_io_state::pointer const &result)
  { return invoke_one_helper<T, IO, Args...> (func, result); }

  Callback const callback_;
};

template<typename Failure, typename Callback>
std::size_t const states::waiting_t<Failure, Callback>::TAG
    = types::make<states::waiting_t<Failure, Callback>> ();


template<typename Failure, typename ...Success>
template<typename BindF>
typename std::result_of<BindF (Success...)>::type
basic_io<Failure, Success...>::operator ->* (BindF func) const
{
  SCOPE;
  typedef typename std::result_of<BindF (Success...)>::type result_type;

  switch (state ())
    {
    case io_state::success:
      LOG (INFO) << "[_/" << this->id () << "] State is success; immediately calling next function";
      return type_cast<states::success_t<Success...> &> (*state_->io)
        ->* func;
    case io_state::failure:
      LOG (INFO) << "[_/" << this->id () << "] State is failure; immediately propagating error";
      return result_type (&type_cast<states::failure_t<Failure> &> (*state_->io));
    case io_state::waiting:
      {
        result_type blocked (make_ptr<states::waiting_t<Failure, BindF>> (func));

        LOG (INFO) << "[_/" << this->id () << "] Adding blocked IO: [" << blocked.id () << "]";
        this->state_->blocked.push_back (blocked);

        return std::move (blocked);
      }
    }
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


static io_state
aggregate_states ()
{
  return io_state::success;
}

template<typename Head, typename ...Tail>
io_state
aggregate_states (Head head, Tail const &...tail)
{
  return head.state () || aggregate_states (tail...);
}


template<typename Failure, typename ...Success>
static basic_io<Failure, Success...>
aggregate_success ()
{
  return success ();
}

template<typename Failure, typename ...Success, typename ...Tail>
static basic_io<Failure, Success...>
aggregate_success (basic_io<Failure, Success...> const &head, Tail const &...tail)
{
  // TODO: aggregate
  (void) head;
  return aggregate_success<Failure, Success...> (tail...);
}


template<typename Failure, typename ...Success>
static basic_io<Failure, Success...>
aggregate_failure ()
{
  LOG (FATAL) << "Failed to aggregate failure: no failures found";
}

template<typename Failure, typename ...Success, typename ...Tail>
static basic_io<Failure, Success...>
aggregate_failure (basic_io<Failure, Success...> const &head, Tail const &...tail)
{
  if (head.state () == io_state::failure)
    return head;
  return aggregate_failure<Failure, Success...> (tail...);
}


template<typename Failure, typename ...Blocking>
//states::blocked_t<Blocking...>
basic_io<Failure>
combine (Blocking const &...blocking)
{
  SCOPE;
  LOG_ASSERT (sizeof... (Blocking) > 1);
  LOG (INFO) << "Combining " << sizeof... (Blocking) << " IOs";

  basic_io<Failure> waiting (make_ptr<states::success_t<>> ());

  auto callback = [=] () mutable -> basic_io<Failure> {
    switch (aggregate_states (blocking...))
      {
      case io_state::success:
        LOG (INFO) << "All states were successful; aggregating results";
        waiting.reset ();
        return aggregate_success (blocking...);
      case io_state::failure:
        LOG (INFO) << "At least one state failed; returning first failure";
        waiting.reset ();
        return aggregate_failure (blocking...);
      case io_state::waiting:
        LOG (INFO) << "At least one state is still waiting; returning self";
        return waiting;
      }
  };

  waiting = basic_io<Failure>
    (make_ptr<states::waiting_t<Failure, decltype (callback)>> (std::move (callback)));

  waiting.add_blocked (blocking...);

  return waiting;
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



/******************************************************************************
 * event_loop
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


void type_name (std::string &name, shared_fd const &)
{ name += "shared_fd"; }


std::ostream &
operator << (std::ostream &os, shared_fd const &fd)
{
  return os << fd->fd;
}


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
open (char const *pathname)
{
  //return success (make_ptr<file_descriptor> (STDIN_FILENO));

  int fd = ::open (pathname, 0);
  if (fd < 0)
    return failure (SystemError (errno));

  return success (make_ptr<file_descriptor> (fd));
}


io<std::vector<uint8_t>>
read (shared_fd fd, std::size_t count)
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


}


#include "lwt/io.h"
#include "lwt/logging.h"
#include <gtest/gtest.h>


namespace lwt
{


TEST (IO, OpenClose) {
  io<> program = open ("/dev/stdin")
    ->* [] (shared_fd fd) -> io<> {
      LOG_ASSERT (fd->fd >= 0);
      return success ();
    };

  default_loop.run (program);
}


TEST (IO, Read) {
  typedef std::vector<uint8_t> byte_vec;

  io<> program = open ("/dev/stdin")

    ->* [] (shared_fd fd) -> io<byte_vec> {
      return read (fd, 10);
    }

    ->* [] (byte_vec const &data) -> io<> {
      LOG_ASSERT (data.size () == 10);
      return success ();
    };

  default_loop.run (program);
}


TEST (IO, DirectFailure) {
  typedef std::vector<uint8_t> byte_vec;

  io<> program = open ("/dev/stdin")

    ->* [] (shared_fd fd) -> io<byte_vec> {
      LOG_ASSERT (fd->fd >= 0);
      return failure (SystemError (0));
    }

    ->* [] (byte_vec const &data) -> io<> {
      LOG_ASSERT (data.size () == 10);
      return success ();
    };

  default_loop.run (program);
}


TEST (IO, WaitingFailure) {
  typedef std::vector<uint8_t> byte_vec;

  io<> program = open ("/dev/stdin")

    ->* [] (shared_fd fd) -> io<byte_vec> {
      return read (fd, 10)
        ->* [] (byte_vec const &data) -> io<byte_vec> {
          LOG (INFO) << "Got " << data.size () << " bytes";
          LOG_ASSERT (data.size () == 10);
          return failure (SystemError (0));
        };
    }

    ->* [] (byte_vec const &data) -> io<> {
      LOG_ASSERT (data.size () == 10);
      return success ();
    };

  default_loop.run (program);
}


TEST (IO, ReadMultiplex) {
  typedef std::vector<uint8_t> byte_vec;

  io<> program = open ("/dev/stdin")
    ->* [] (shared_fd fd) -> io<> {
      io<byte_vec> waiting_read = read (fd, 10);

      // First waiting operation on first read.
      io<byte_vec> waiting1_1 = waiting_read
        ->* [] (byte_vec const &buffer1) -> io<> {
          SCOPE;
          LOG (INFO) << "got buffer 1: " << buffer1.size ();
          return success ();
        }

        ->* deferred (read, fd, 10);

      // First waiting operation on second read.
      io<> waiting2_1 = waiting1_1
        ->* [] (byte_vec const &buffer2) -> io<> {
          SCOPE;
          LOG (INFO) << "got buffer 2: " << buffer2.size ();
          return success ();
        };


      // Second waiting operation on second read.
      io<> waiting2_2 = waiting1_1
        ->* [] (byte_vec const &buffer2) -> io<> {
          SCOPE;
          LOG (INFO) << "got buffer 2 again: " << buffer2.size ();
          return success ();
        };


      // Second waiting operation on first read.
      io<> waiting1_2 = waiting_read
        ->* [] (byte_vec const &buffer1) -> io<> {
          SCOPE;
          LOG (INFO) << "got buffer 1 again: " << buffer1.size ();
          return success ();
        };

      return combine<SystemError> (waiting2_1, waiting2_2, waiting1_2);
    };

  default_loop.run (program);
}


}
