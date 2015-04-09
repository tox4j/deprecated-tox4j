#pragma once

#include <tox/tox.h>


#include <memory>
#include <cassert>


namespace tox
{

  namespace detail
  {

    template<typename ...Args>
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


    template<typename Sig>
    struct mk_tox_cb;

    // XXX: why don't variadic templates work here?
    template<typename ...Args>
    struct mk_tox_cb<void (Tox *, Args..., void *)>
      : tox_cb<Args...>
    { };

    template<typename Arg1>
    struct mk_tox_cb<void (Tox *, Arg1, void *)>
      : tox_cb<Arg1>
    { };

    template<typename Arg1, typename Arg2>
    struct mk_tox_cb<void (Tox *, Arg1, Arg2, void *)>
      : tox_cb<Arg1, Arg2>
    { };

    template<typename Arg1, typename Arg2, typename Arg3>
    struct mk_tox_cb<void (Tox *, Arg1, Arg2, Arg3, void *)>
      : tox_cb<Arg1, Arg2, Arg3>
    { };

    template<typename Arg1, typename Arg2, typename Arg3, typename Arg4>
    struct mk_tox_cb<void (Tox *, Arg1, Arg2, Arg3, Arg4, void *)>
      : tox_cb<Arg1, Arg2, Arg3, Arg4>
    { };

    template<typename Arg1, typename Arg2, typename Arg3, typename Arg4, typename Arg5>
    struct mk_tox_cb<void (Tox *, Arg1, Arg2, Arg3, Arg4, Arg5, void *)>
      : tox_cb<Arg1, Arg2, Arg3, Arg4, Arg5>
    { };

    template<typename Arg1, typename Arg2, typename Arg3, typename Arg4, typename Arg5, typename Arg6>
    struct mk_tox_cb<void (Tox *, Arg1, Arg2, Arg3, Arg4, Arg5, Arg6, void *)>
      : tox_cb<Arg1, Arg2, Arg3, Arg4, Arg5, Arg6>
    { };


    template<typename Sig, void Set (Tox *, Sig, void *)>
    struct cb
      : mk_tox_cb<Sig>::template func<Set>
    { };

  }

#define CALLBACK(NAME)  using callback_##NAME = detail::cb<tox_##NAME##_cb, tox_callback_##NAME>
  CALLBACK (self_connection_status);
  CALLBACK (friend_name);
  CALLBACK (friend_status_message);
  CALLBACK (friend_status);
  CALLBACK (friend_connection_status);
  CALLBACK (friend_typing);
  CALLBACK (friend_read_receipt);
  CALLBACK (friend_request);
  CALLBACK (friend_message);
  CALLBACK (file_recv_control);
  CALLBACK (file_chunk_request);
  CALLBACK (file_recv);
  CALLBACK (file_recv_chunk);
  CALLBACK (friend_lossy_packet);
  CALLBACK (friend_lossless_packet);
#undef CALLBACK


  namespace detail
  {

    template<
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


    template<typename UserData, typename ...Callbacks>
    struct set_callbacks;

    template<typename UserData>
    struct set_callbacks<UserData>
    {
      static std::unique_ptr<UserData>
      set (Tox *, std::unique_ptr<UserData> user_data)
      {
        return user_data;
      }
    };

    template<typename UserData, typename Head, typename ...Tail>
    struct set_callbacks<UserData, Head, Tail...>
    {
      static std::unique_ptr<UserData>
      set (Tox *tox, std::unique_ptr<UserData> user_data)
      {
        return set_callbacks<UserData, Tail...>::set (tox, Head::set (tox, std::move (user_data)));
      }
    };


    template<typename UserData, typename ...Callbacks>
    struct callback_setter
    {
      template<
        typename Cb,
        typename Cb::template type<UserData> Sig
      >
      callback_setter<UserData, Callbacks..., setter<UserData, Cb, Sig>>
      set () &&
      { return { std::move (user_data) }; }

      std::unique_ptr<UserData>
      set (Tox *tox) &&
      {
        return set_callbacks<UserData, Callbacks...>::set (tox, std::move (user_data));
      }

      std::unique_ptr<UserData> user_data;
    };

  }


  template<typename UserData>
  detail::callback_setter<UserData>
  callbacks (std::unique_ptr<UserData> user_data)
  {
    return { std::move (user_data) };
  }

}
