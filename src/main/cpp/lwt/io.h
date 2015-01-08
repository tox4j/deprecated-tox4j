#pragma once

#include "partial.h"

#include <boost/intrusive_ptr.hpp>

#include <vector>


namespace lwt
{
  template<typename ...Types>
  struct basic_io;


  template<typename T>
  using ptr = boost::intrusive_ptr<T>;

  template<typename ...Types>
  using io = ptr<basic_io<Types...>>;


  enum class error
  {
    Unknown,
  };


  struct io_base
  {
    friend void intrusive_ptr_add_ref (io_base *io);
    friend void intrusive_ptr_release (io_base *io);

    virtual ptr<io_base> step (bool &done) = 0;

  private:
    std::size_t refcount;
  };


  template<typename ...Types>
  struct basic_io
    : io_base
  {
    template<typename BindF>
    struct bind;

  private:
  };


  template<typename ...Types>
  struct io_success
    : basic_io<Types...>
  {
    explicit io_success (Types const &...values)
      : data (values...)
    { }

    template<typename BindF, std::size_t ...S>
    typename std::result_of<BindF (Types const &...)>::type
    apply (seq<S...>, BindF const &func) const
    {
      return func (std::get<S> (data)...);
    }

  private:
    ptr<io_base> step (bool &done) override
    {
      done = true;
      return this;
    }

    std::tuple<Types...> data;
  };


  template<typename BindF, typename ...Types>
  struct io_waiting
    : basic_io<Types...>
  {
    io_waiting (BindF const &func, ptr<io_base> const &blocking)
      : func_ (func)
      , blocking_ (blocking)
    { }

  private:
    ptr<io_base> step (bool &done) override
    {
      blocking_ = blocking_->step (done);
      if (done)
        return func_ (blocking_);
      return this;
    }

    BindF func_;
    ptr<io_base> blocking_;
  };


  template<typename ...Types>
  template<typename BindF>
  struct basic_io<Types...>::bind
  {
    typedef io_waiting<BindF, Types...> type;
  };


  template<typename BindF, typename ...Types>
  typename std::result_of<BindF (Types const &...)>::type
  operator ->* (io<Types...> const &io, BindF const &func)
  {
    typedef typename std::result_of<BindF (Types const &...)>::type::element_type io_type;
    auto thunk = [io, func] (ptr<io_base> const &result) {
      return static_cast<io_success<Types...> const &> (*result)
        .template apply<BindF> (make_seq<sizeof... (Types)> (), func);
    };
    return ptr<io_type> (new typename io_type::template bind<decltype (thunk)>::type (thunk, io));
  }


  template<typename ...Types>
  io<Types...>
  success (Types const &...values)
  {
    return io<Types...> (new io_success<Types...> (values...));
  }


  void eval (io<> io);

  io<> unit ();

  io<int> open (char const *pathname);

  io<std::vector<uint8_t>> read (int fd, std::size_t count, std::vector<uint8_t> &&buffer = std::vector<uint8_t> ());
}
