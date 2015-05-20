#pragma once

#include <cassert>
#include <memory>

namespace tox
{

  namespace detail
  {

    template<typename Tox, typename ...Args>
    struct tox_cb
    {
      template<void Set (Tox *, void (*)(Tox *, Args..., void *), void *)>
      struct func
      {
        template<typename UserData>
        using type = void (*) (Tox *, Args..., UserData &);

        template<typename UserData, type<UserData> Callback>
        static void
        invoke (Tox *tox, Args ...args, void *user_data)
        {
          Callback (tox, args..., *static_cast<UserData *> (user_data));
        }

        template<typename UserData, type<UserData> Callback>
        static std::unique_ptr<UserData>
        set (Tox *tox, std::unique_ptr<UserData> user_data)
        {
          assert (user_data.get () != nullptr);
          Set (tox, invoke<UserData, Callback>, user_data.get ());
          return user_data;
        }
      };
    };


    template<typename Tox, typename Sig>
    struct mk_tox_cb;

    // XXX: why don't variadic templates work here?
    template<typename Tox, typename ...Args>
    struct mk_tox_cb<Tox, void (Tox *, Args..., void *)>
      : tox_cb<Tox, Args...>
    { };

    template<typename Tox, typename Arg1>
    struct mk_tox_cb<Tox, void (Tox *, Arg1, void *)>
      : tox_cb<Tox, Arg1>
    { };

    template<typename Tox, typename Arg1, typename Arg2>
    struct mk_tox_cb<Tox, void (Tox *, Arg1, Arg2, void *)>
      : tox_cb<Tox, Arg1, Arg2>
    { };

    template<typename Tox, typename Arg1, typename Arg2, typename Arg3>
    struct mk_tox_cb<Tox, void (Tox *, Arg1, Arg2, Arg3, void *)>
      : tox_cb<Tox, Arg1, Arg2, Arg3>
    { };

    template<typename Tox, typename Arg1, typename Arg2, typename Arg3, typename Arg4>
    struct mk_tox_cb<Tox, void (Tox *, Arg1, Arg2, Arg3, Arg4, void *)>
      : tox_cb<Tox, Arg1, Arg2, Arg3, Arg4>
    { };

    template<typename Tox, typename Arg1, typename Arg2, typename Arg3, typename Arg4, typename Arg5>
    struct mk_tox_cb<Tox, void (Tox *, Arg1, Arg2, Arg3, Arg4, Arg5, void *)>
      : tox_cb<Tox, Arg1, Arg2, Arg3, Arg4, Arg5>
    { };

    template<typename Tox, typename Arg1, typename Arg2, typename Arg3, typename Arg4, typename Arg5, typename Arg6>
    struct mk_tox_cb<Tox, void (Tox *, Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, void *)>
      : tox_cb<Tox, Arg1, Arg2, Arg3, Arg4, Arg5, Arg6>
    { };


    template<typename Tox, typename Sig, void Set (Tox *, Sig, void *)>
    struct cb
      : mk_tox_cb<Tox, Sig>::template func<Set>
    { };

  }

  namespace detail
  {

    template<
      typename Tox,
      typename UserData,
      typename Cb,
      typename Cb::template type<UserData> Sig
    >
    struct setter
    {
      static std::unique_ptr<UserData>
      set (Tox *tox, std::unique_ptr<UserData> user_data)
      {
        return Cb::template set<UserData, Sig> (tox, std::move (user_data));
      }
    };


    template<typename Tox, typename UserData, typename ...Callbacks>
    struct set_callbacks;

    template<typename Tox, typename UserData>
    struct set_callbacks<Tox, UserData>
    {
      static std::unique_ptr<UserData>
      set (Tox *, std::unique_ptr<UserData> user_data)
      {
        return user_data;
      }
    };

    template<typename Tox, typename UserData, typename Head, typename ...Tail>
    struct set_callbacks<Tox, UserData, Head, Tail...>
    {
      static std::unique_ptr<UserData>
      set (Tox *tox, std::unique_ptr<UserData> user_data)
      {
        return set_callbacks<Tox, UserData, Tail...>::set (tox, Head::set (tox, std::move (user_data)));
      }
    };


    template<typename Tox, typename UserData, typename ...Callbacks>
    struct callback_setter
    {
      template<
        typename Cb,
        typename Cb::template type<UserData> Sig
      >
      callback_setter<Tox, UserData, Callbacks..., setter<Tox, UserData, Cb, Sig>>
      set () &&
      { return { std::move (user_data) }; }

      std::unique_ptr<UserData>
      set (Tox *tox) &&
      {
        return set_callbacks<Tox, UserData, Callbacks...>::set (tox, std::move (user_data));
      }

      std::unique_ptr<UserData> user_data;
    };

  }


  template<typename Tox, typename UserData>
  detail::callback_setter<Tox, UserData>
  callbacks (std::unique_ptr<UserData> user_data)
  {
    return { std::move (user_data) };
  }

}
