#pragma once

#include "lwt/logging.h"

#include "lwt/optional.h"
#include "lwt/variant.h"
#include "lwt/types.h"

#include <boost/intrusive_ptr.hpp>

#include <algorithm>
#include <memory>
#include <vector>

#include <fcntl.h>
#include <sys/stat.h>
#include <sys/types.h>


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

io_state operator || (io_state lhs, io_state rhs);
std::ostream &operator << (std::ostream &os, io_state state);

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


namespace states
{
  template<typename ...Success>
  struct success_t;

  template<typename Failure>
  struct failure_t;

  template<typename Failure, typename Callback>
  struct waiting_t;
}


struct basic_io_base
{
  static std::size_t object_count;

  explicit basic_io_base (basic_io_state::pointer const &io)
    : state_ (make_ptr<data> (io))
  {
    SCOPE;
    LOG_ASSERT (io != nullptr);
    LOG (INFO) << *this << "Creating io_base in state " << io->state ();
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


static inline io_state
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


}
