#pragma once

#include "partial.h"

#include <boost/intrusive_ptr.hpp>

#include <vector>


namespace lwt
{
  template<typename T>
  using ptr = boost::intrusive_ptr<T>;


  enum class error
  {
    Unknown,
  };


  struct io_base
  {
    friend void intrusive_ptr_add_ref (io_base *io);
    friend void intrusive_ptr_release (io_base *io);

  private:
    std::size_t refcount;
  };


  template<typename ...Types>
  struct basic_io
    : io_base
  {
    template<typename SuccessF, typename FailureF>
    void eval (SuccessF &&success, FailureF &&failure)
    {
    }

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

  private:
    std::tuple<Types...> data;
  };


  template<typename BindF, typename ...Types>
  struct io_waiting
    : basic_io<Types...>
  {
    io_waiting (BindF &&func, ptr<io_base> const &blocking)
      : data { func, blocking }
    { }

  private:
    struct waiting
    {
      BindF func;
      ptr<io_base> blocking;
    };

    waiting data;
  };


  template<typename ...Types>
  template<typename BindF>
  struct basic_io<Types...>::bind
  {
    typedef io_waiting<BindF, Types...> type;
  };


  template<typename ...Types>
  using io = ptr<basic_io<Types...>>;


  template<typename BindF, typename ...Types>
  typename std::result_of<BindF (Types const &...)>::type
  operator ->* (io<Types...> const &io, BindF &&func)
  {
    typedef typename std::result_of<BindF (Types const &...)>::type::element_type io_type;
    return ptr<io_type> (new typename io_type::template bind<BindF>::type (std::move (func), io));
  }


  template<typename ...Types>
  io<typename std::remove_reference<Types>::type...>
  success (Types &&...values)
  {
    return io<typename std::remove_reference<Types>::type...>
      (new io_success<typename std::remove_reference<Types>::type...>
         (std::forward<Types> (values)...));
  }


  io<void> unit ();

  io<int> open (char const *pathname);

  io<std::vector<uint8_t>> read (int fd, std::size_t count, std::vector<char> &&buffer = std::vector<char> ());
}
