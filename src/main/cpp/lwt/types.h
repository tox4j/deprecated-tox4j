#pragma once

#include <cstring>

#include <string>
#include <vector>

namespace lwt
{
  template<typename T>
  T &type_name_declval ()
  { return *static_cast<typename std::remove_reference<T>::type *> (nullptr); }

  template<typename ...Types>
  void type_name (std::string &name);

  static inline void type_name (std::string &name,   int8_t &) { name +=   "int8_t"; }
  static inline void type_name (std::string &name,  uint8_t &) { name +=  "uint8_t"; }
  static inline void type_name (std::string &name,  int16_t &) { name +=  "int16_t"; }
  static inline void type_name (std::string &name, uint16_t &) { name += "uint16_t"; }
  static inline void type_name (std::string &name,  int32_t &) { name +=  "int32_t"; }
  static inline void type_name (std::string &name, uint32_t &) { name += "uint32_t"; }
  static inline void type_name (std::string &name,  int64_t &) { name +=  "int64_t"; }
  static inline void type_name (std::string &name, uint64_t &) { name += "uint64_t"; }

  template<typename T>
  static void type_name (std::string &name, std::vector<T> const &)
  {
    name += "vector<";
    type_name<T> (name);
    name += ">";
  }


  template<typename T, typename Result, typename ...Args>
  void
  type_name (std::string &name, Result (T::* const &) (Args...))
  {
    type_name<Result> (name);
    //name += "($_0::*)";
    name += "(";
    type_name<Args...> (name);
    name += ")";
  }

  template<typename T, typename Result, typename ...Args>
  void
  type_name (std::string &name, Result (T::* const &) (Args...) const)
  {
    type_name<Result> (name);
    //name += "($_0::*)";
    name += "(";
    type_name<Args...> (name);
    name += ") const";
  }

  template<typename T>
  decltype (void (&T::operator ()))
  type_name (std::string &name, T &)
  {
    type_name (name, &T::operator ());
  }


  static inline void type_name_rec (std::string &) { }

  template<typename Head, typename ...Tail>
  void
  type_name_rec (std::string &name, Head &head, Tail &...tail)
  {
    type_name (name, head);
    if (sizeof... (tail) > 0)
      name += ", ";
    type_name_rec (name, tail...);
  }

  template<typename ...Types>
  void
  type_name (std::string &name)
  {
    type_name_rec (name, type_name_declval<Types> ()...);
  }
}


namespace lwt
{
  struct types
  {
#if 0
    template<typename ...T>
    static std::string name ()
    {
      char const *func = strchr (__PRETTY_FUNCTION__, '=') + 2;
      return std::string (func, func + strlen (func) - 1);
    }
#else
    template<typename ...Types>
    static std::string name ()
    {
      std::string name;
      type_name<Types...> (name);
      return name;
    }
#endif

    template<typename T>
    static std::size_t make ()
    {
      names ().push_back (name<T> ());
      return names ().size () - 1;
    }

    static std::string const &name (std::size_t tag)
    {
      return names ()[tag];
    }

  private:
    static std::vector<std::string> &names ()
    {
      static std::vector<std::string> names;
      return names;
    }
  };


  template<typename To, typename From>
  To type_cast (From &&from)
  {
    std::size_t const TAG = std::remove_reference<To>::type::TAG;
    if (from.tag != TAG)
      LOG (FATAL) << "Tried to cast " << types::name (from.tag) << " to " << types::name (TAG);
    return static_cast<To> (from);
  }
}
