template<typename Func>
typename std::result_of<Func(tox_traits::subsystem *, Events &)>::type
with_instance(JNIEnv *env, jint instance_number, Func func)
{
    typedef typename std::result_of<Func(tox_traits::subsystem *, Events &)>::type return_type;

    if (instance_number == 0) {
        throw_illegal_state_exception(env, instance_number, "Function called on incomplete object");
        return default_value<return_type>();
    }

    std::unique_lock<std::mutex> lock(instance_manager<tox_traits>::self.mutex);
    if (!instance_manager<tox_traits>::self.isValid(instance_number)) {
        throw_tox_killed_exception(env, instance_number, "Tox function invoked on invalid tox instance");
        return default_value<return_type>();
    }

    auto const &instance = instance_manager<tox_traits>::self[instance_number];

    if (!instance.isLive()) {
        throw_tox_killed_exception(env, instance_number, "Tox function invoked on killed tox instance");
        return default_value<return_type>();
    }

    return instance.with_lock([&] {
        tox_traits::subsystem *tox = instance.tox.get();
        Events &events = *instance.events;
        lock.unlock();

        return func(tox, events);
    });
}


template<typename ErrorFunc, typename SuccessFunc, typename ToxFunc, typename... Args>
tox_success_t<SuccessFunc, ToxFunc, Args...>
with_error_handling(JNIEnv *env, char const *method, ErrorFunc error_func, SuccessFunc success_func, ToxFunc tox_func, Args ...args)
{
    tox_error_t<ToxFunc> error;
    auto value = tox_func(args..., &error);
    ErrorHandling result = error_func(error);
    switch (result.result) {
        case ErrorHandling::SUCCESS:
            return success_func(value);
        case ErrorHandling::FAILURE:
            throw_tox_exception(env, tox_traits::module, method, result.error);
            break;
        case ErrorHandling::UNHANDLED:
            throw_illegal_state_exception(env, error, "Unknown error code");
            break;
    }

    return default_value<tox_success_t<SuccessFunc, ToxFunc, Args...>>();
}


template<typename ErrorFunc, typename SuccessFunc, typename ToxFunc, typename... Args>
tox_success_t<SuccessFunc, ToxFunc, tox_traits::subsystem *, Args...>
with_instance(JNIEnv *env, jint instanceNumber, char const *method, ErrorFunc error_func, SuccessFunc success_func, ToxFunc tox_func, Args ...args)
{
    return with_instance(env, instanceNumber, [=](tox_traits::subsystem *tox, Events &events) {
        (void)events;
        return with_error_handling(env, method, error_func, success_func, tox_func, tox, args...);
    });
}
