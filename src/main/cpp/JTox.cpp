#include "Tox4j.h"

/*
 * Do setup here. Caching of needed java method IDs etc should be done in this function. It is guaranteed to be called
 * when the library is loaded, and nothing else will be called before this function is called.
 */
jint JNI_OnLoad(JavaVM *, void *) {
    return JNI_VERSION_1_4;
}

JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_destroyAll(JNIEnv *, jclass) {
    std::unique_lock<std::mutex> lock(ToxInstances::self.mutex);
    ToxInstances::self.destroyAll();
}

JNIEXPORT jint JNICALL Java_im_tox_tox4j_Tox4j_toxNew(JNIEnv *env, jclass, jboolean ipv6enabled, jboolean udpDisabled,
    jboolean proxyEnabled, jstring proxyAddress, jint proxyPort) {
    auto opts = Tox_Options();
    opts.ipv6enabled = (uint8_t) ipv6enabled;
    opts.udp_disabled = (uint8_t) udpDisabled;
    if (proxyEnabled) {
        tox4j_assert(proxyAddress != nullptr, env, "Proxy Address cannot be null when proxy is enabled");
        opts.proxy_enabled = true;
        strncpy(opts.proxy_address, UTFChars(env, proxyAddress), sizeof(opts.proxy_address) - 1);
        opts.proxy_port = (uint16_t) proxyPort;
    }

    std::unique_ptr<Tox, ToxDeleter> tox(tox_new(&opts));
    if (tox == nullptr) {
        return -1;
    }

    // We can create the new instance outside instances' critical section.
    Tox4jStruct instance {
        std::move(tox),
        std::unique_ptr<ToxEvents>(new ToxEvents),
        std::unique_ptr<std::mutex>(new std::mutex)
    };

    // This lock guards the instance manager.
    std::lock_guard<std::mutex> lock(ToxInstances::self.mutex);
    return ToxInstances::self.add(std::move(instance));
}

JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_kill(JNIEnv *env, jclass, jint instance_number) {
    std::lock_guard<std::mutex> lock(ToxInstances::self.mutex);

    if (instance_number < 0) {
        throw_illegal_state_exception(env, instance_number, "Tox instance out of range");
        return;
    }

    if (!ToxInstances::self.isValid(instance_number)) {
        throw_tox_killed_exception(env, instance_number, "close called on invalid instance");
        return;
    }

    // After this move, the pointers in instance_vector[instance_number] will all be nullptr...
    Tox4jStruct dying(ToxInstances::self.remove(instance_number));

    // ... so that this check will fail, if the function is called twice on the same instance.
    if (!dying.tox) {
        throw_tox_killed_exception(env, instance_number, "close called on already closed instance");
        return;
    }

    std::lock_guard<std::mutex> ilock(*dying.mutex);
}

JNIEXPORT void JNICALL Java_im_tox_tox4j_Tox4j_finalize(JNIEnv *env, jclass, jint instance_number) {
    if (instance_number == 0) {
        // This can happen when an exception is thrown from the constructor, giving this object an invalid state,
        // containing instance_number = 0.
        return;
    }

    if (ToxInstances::self.empty()) {
        throw_illegal_state_exception(env, instance_number, "Tox instance manager is empty");
        return;
    }

    std::lock_guard<std::mutex> lock(ToxInstances::self.mutex);
    if (!ToxInstances::self.isValid(instance_number)) {
        throw_illegal_state_exception(env, instance_number,
            "Tox instance out of range (max: " + std::to_string(ToxInstances::self.size() - 1) + ")");
        return;
    }

    // An instance should never be on this list twice.
    if (ToxInstances::self.isFree(instance_number)) {
        throw_illegal_state_exception(env, instance_number, "Tox instance already on free list");
        return;
    }

    ToxInstances::self.setFree(instance_number);
}

JNIEXPORT jint JNICALL Java_im_tox_tox4j_Tox4j_bootstrap(JNIEnv *env, jclass, jint instance_number,
    jstring address, jint port, jbyteArray public_key) {
    tox4j_assert(address != nullptr, env, "Bootstrap address cannot be null");
    tox4j_assert(public_key != nullptr, env, "Public key cannot be null");
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &) {
        return tox_bootstrap_from_address(tox, UTFChars(env, address), (uint16_t) port, ByteArray(env, public_key));
    });
}

JNIEXPORT jint JNICALL Java_im_tox_tox4j_Tox4j_addTcpRelay(JNIEnv *env, jclass, jint instance_number, jstring address,
    jint port, jbyteArray public_key) {
    tox4j_assert(address != nullptr, env, "Relay address cannot be null");
    tox4j_assert(public_key != nullptr, env, "Public key cannot be null");
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &) {
        return tox_add_tcp_relay(tox, UTFChars(env, address), (uint16_t) port, ByteArray(env, public_key));
    });
}

JNIEXPORT jboolean JNICALL Java_im_tox_tox4j_Tox4j_isConnected(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &) { return tox_isconnected(tox); });
}

JNIEXPORT jint JNICALL Java_im_tox_tox4j_Tox4j_doInterval(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &) { return tox_do_interval(tox); });
}

JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_Tox4j_toxDo(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &events) {
        tox_do(tox);

        std::vector<char> buffer(events.ByteSize());
        events.SerializeToArray(buffer.data(), buffer.size());
        events.Clear();

        return toByteArray(env, buffer);
    });
}
JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_Tox4j_save(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &) {
        std::vector<uint8_t> buffer(tox_size(tox));
        tox_save(tox, buffer.data());

        return toByteArray(env, buffer);
    });
}

JNIEXPORT jboolean JNICALL Java_im_tox_tox4j_Tox4j_load(JNIEnv *env, jclass, jint instance_number, jbyteArray data) {
    tox4j_assert(data != nullptr, env, "Data cannot be null");
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &) {
        ByteArray bytes(env, data);
        if (tox_load(tox, bytes, (uint32_t) bytes.length()) == 0) {
            return JNI_TRUE;
        }
        return JNI_FALSE;
    });
}

JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_Tox4j_getAddress(JNIEnv *env, jclass, jint instance_number) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &) {
        std::vector<uint8_t> address(TOX_FRIEND_ADDRESS_SIZE);
        tox_get_address(tox, address.data());

        return toByteArray(env, address);
    });
}

JNIEXPORT jint JNICALL Java_im_tox_tox4j_Tox4j_addFriend(JNIEnv *env, jclass, jint instance_number, jbyteArray address,
    jbyteArray message) {
    tox4j_assert(address != nullptr, env, "Address cannot be null");
    tox4j_assert(message != nullptr, env, "Message cannot be null");
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &) {
        ByteArray messageBytes(env, message);
        return tox_add_friend(tox, ByteArray(env, address), messageBytes, (uint16_t) messageBytes.length());
    });
}

JNIEXPORT jint JNICALL Java_im_tox_tox4j_Tox4j_addFriendNoRequest(JNIEnv *env, jclass, jint instance_number,
    jbyteArray clientId) {
    tox4j_assert(clientId != nullptr, env, "Client ID cannot be null");
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &) {
        return tox_add_friend_norequest(tox, ByteArray(env, clientId));
    });
}

JNIEXPORT jint JNICALL Java_im_tox_tox4j_Tox4j_getFriendNumber(JNIEnv *env, jclass, jint instance_number,
    jbyteArray clientId) {
    tox4j_assert(clientId != nullptr, env, "Client ID cannot be null");
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &) {
        return tox_get_friend_number(tox, ByteArray(env, clientId));
    });
}


JNIEXPORT jbyteArray JNICALL Java_im_tox_tox4j_Tox4j_getClientId(JNIEnv *env, jclass, jint instance_number,
    jint friendnumber) {
    return with_instance(env, instance_number, [=](Tox *tox, ToxEvents &) {
        std::vector<uint8_t> buffer(TOX_CLIENT_ID_SIZE);
        jbyteArray result = nullptr;
        if (tox_get_client_id(tox, friendnumber, buffer.data()) != -1) {
            result = toByteArray(env, buffer);
        }
        return result;
    });
}
