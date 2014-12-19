#include "tox4j/Tox4j.h"
#include "jniutil.h"

static void tox4j_connection_status_cb(Tox *tox, TOX_CONNECTION connection_status, void *user_data)
{
    unused(tox);
    ToxEvents &events = *static_cast<ToxEvents *>(user_data);
    auto msg = events.add_connectionstatus();

    using im::tox::tox4j::proto::ConnectionStatus;
    switch (connection_status) {
        case TOX_CONNECTION_NONE:
            msg->set_connectionstatus(ConnectionStatus::NONE);
            break;
        case TOX_CONNECTION_UDP:
            msg->set_connectionstatus(ConnectionStatus::UDP);
            break;
        case TOX_CONNECTION_TCP:
            msg->set_connectionstatus(ConnectionStatus::TCP);
            break;
    }
}

static void tox4j_friend_name_cb(Tox *tox, uint32_t friend_number, uint8_t const *name, size_t length, void *user_data)
{
    unused(tox);
    ToxEvents &events = *static_cast<ToxEvents *>(user_data);
    auto msg = events.add_friendname();
    msg->set_friendnumber(friend_number);
    msg->set_name(name, length);
}

static void tox4j_friend_status_message_cb(Tox *tox, uint32_t friend_number, uint8_t const *message, size_t length, void *user_data)
{
    unused(tox);
    ToxEvents &events = *static_cast<ToxEvents *>(user_data);
    auto msg = events.add_friendstatusmessage();
    msg->set_friendnumber(friend_number);
    msg->set_message(message, length);
}

static void tox4j_friend_status_cb(Tox *tox, uint32_t friend_number, TOX_STATUS status, void *user_data)
{
    unused(tox);
    ToxEvents &events = *static_cast<ToxEvents *>(user_data);
    auto msg = events.add_friendstatus();
    msg->set_friendnumber(friend_number);

    using im::tox::tox4j::proto::FriendStatus;
    switch (status) {
        case TOX_STATUS_NONE:
            msg->set_status(FriendStatus::NONE);
            break;
        case TOX_STATUS_AWAY:
            msg->set_status(FriendStatus::AWAY);
            break;
        case TOX_STATUS_BUSY:
            msg->set_status(FriendStatus::BUSY);
            break;
    }
}

static void tox4j_friend_connection_status_cb(Tox *tox, uint32_t friend_number, TOX_CONNECTION connection_status, void *user_data)
{
    unused(tox);
    ToxEvents &events = *static_cast<ToxEvents *>(user_data);
    auto msg = events.add_friendconnectionstatus();
    msg->set_friendnumber(friend_number);

    using im::tox::tox4j::proto::FriendConnectionStatus;
    switch (connection_status) {
        case TOX_CONNECTION_NONE:
            msg->set_connectionstatus(FriendConnectionStatus::NONE);
            break;
        case TOX_CONNECTION_UDP:
            msg->set_connectionstatus(FriendConnectionStatus::UDP);
            break;
        case TOX_CONNECTION_TCP:
            msg->set_connectionstatus(FriendConnectionStatus::TCP);
            break;
    }
}

static void tox4j_friend_typing_cb(Tox *tox, uint32_t friend_number, bool is_typing, void *user_data)
{
    unused(tox);
    ToxEvents &events = *static_cast<ToxEvents *>(user_data);
    auto msg = events.add_friendtyping();
    msg->set_friendnumber(friend_number);
    msg->set_istyping(is_typing);
}

static void tox4j_read_receipt_cb(Tox *tox, uint32_t friend_number, uint32_t message_id, void *user_data)
{
    unused(tox);
    ToxEvents &events = *static_cast<ToxEvents *>(user_data);
    auto msg = events.add_readreceipt();
    msg->set_friendnumber(friend_number);
    msg->set_messageid(message_id);
}

static void tox4j_friend_request_cb(Tox *tox, uint8_t const *client_id, /*uint32_t time_delta, */uint8_t const *message, size_t length, void *user_data)
{
    unused(tox);
    ToxEvents &events = *static_cast<ToxEvents *>(user_data);
    auto msg = events.add_friendrequest();
    msg->set_clientid(client_id, TOX_CLIENT_ID_SIZE);
    msg->set_timedelta(0);
    msg->set_message(message, length);
}

static void tox4j_friend_message_cb(Tox *tox, uint32_t friend_number, /*uint32_t time_delta, */uint8_t const *message, size_t length, void *user_data)
{
    unused(tox);
    ToxEvents &events = *static_cast<ToxEvents *>(user_data);
    auto msg = events.add_friendmessage();
    msg->set_friendnumber(friend_number);
    msg->set_timedelta(0);
    msg->set_message(message, length);
}

static void tox4j_friend_action_cb(Tox *tox, uint32_t friend_number, /*uint32_t time_delta, */uint8_t const *action, size_t length, void *user_data)
{
    unused(tox);
    ToxEvents &events = *static_cast<ToxEvents *>(user_data);
    auto msg = events.add_friendaction();
    msg->set_friendnumber(friend_number);
    msg->set_timedelta(0);
    msg->set_action(action, length);
}

static void tox4j_file_control_cb(Tox *tox, uint32_t friend_number, uint32_t file_number, TOX_FILE_CONTROL control, void *user_data)
{
    unused(tox);
    ToxEvents &events = *static_cast<ToxEvents *>(user_data);
    auto msg = events.add_filecontrol();
    msg->set_friendnumber(friend_number);
    msg->set_filenumber(file_number);

    using im::tox::tox4j::proto::FileControl;
    switch (control) {
        case TOX_FILE_CONTROL_RESUME:
            msg->set_control(FileControl::RESUME);
            break;
        case TOX_FILE_CONTROL_PAUSE:
            msg->set_control(FileControl::PAUSE);
            break;
        case TOX_FILE_CONTROL_CANCEL:
            msg->set_control(FileControl::CANCEL);
            break;
    }
}

static void tox4j_file_request_chunk_cb(Tox *tox, uint32_t friend_number, uint32_t file_number, uint64_t position, size_t length, void *user_data)
{
    unused(tox);
    ToxEvents &events = *static_cast<ToxEvents *>(user_data);
    auto msg = events.add_filerequestchunk();
    msg->set_friendnumber(friend_number);
    msg->set_filenumber(file_number);
    msg->set_position(position);
    msg->set_length(length);
}

static void tox4j_file_receive_cb(Tox *tox, uint32_t friend_number, uint32_t file_number, TOX_FILE_KIND kind, uint64_t file_size, uint8_t const *filename, size_t filename_length, void *user_data)
{
    unused(tox);
    ToxEvents &events = *static_cast<ToxEvents *>(user_data);
    auto msg = events.add_filereceive();
    msg->set_friendnumber(friend_number);
    msg->set_filenumber(file_number);

    using im::tox::tox4j::proto::FileReceive;
    switch (kind) {
        case TOX_FILE_KIND_DATA:
            msg->set_kind(FileReceive::DATA);
            break;
        case TOX_FILE_KIND_AVATAR:
            msg->set_kind(FileReceive::AVATAR);
            break;
    }

    msg->set_filesize(file_size);
    msg->set_filename(filename, filename_length);
}

static void tox4j_file_receive_chunk_cb(Tox *tox, uint32_t friend_number, uint32_t file_number, uint64_t position, uint8_t const *data, size_t length, void *user_data)
{
    unused(tox);
    ToxEvents &events = *static_cast<ToxEvents *>(user_data);
    auto msg = events.add_filereceivechunk();
    msg->set_friendnumber(friend_number);
    msg->set_filenumber(file_number);
    msg->set_position(position);
    msg->set_data(data, length);
}

static void tox4j_friend_lossy_packet_cb(Tox *tox, uint32_t friend_number, uint8_t const *data, size_t length, void *user_data)
{
    unused(tox);
    ToxEvents &events = *static_cast<ToxEvents *>(user_data);
    auto msg = events.add_friendlossypacket();
    msg->set_friendnumber(friend_number);
    msg->set_data(data, length);
}

static void tox4j_friend_lossless_packet_cb(Tox *tox, uint32_t friend_number, uint8_t const *data, size_t length, void *user_data)
{
    unused(tox);
    ToxEvents &events = *static_cast<ToxEvents *>(user_data);
    auto msg = events.add_friendlosslesspacket();
    msg->set_friendnumber(friend_number);
    msg->set_data(data, length);
}


/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    destroyAll
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxCoreImpl_destroyAll
  (JNIEnv *, jclass)
{
    std::unique_lock<std::mutex> lock(InstanceManager::self.mutex);
    InstanceManager::self.destroyAll();
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxNew
 * Signature: (ZZILjava/lang/String;I)I
 */
JNIEXPORT jint JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxNew
  (JNIEnv *env, jclass, jboolean ipv6Enabled, jboolean udpEnabled, jint proxyType, jstring proxyAddress, jint proxyPort)
{
    assert(proxyType >= 0);
    assert(proxyPort >= 0);
    assert(proxyPort <= 65535);

    struct Tox_Options_Deleter {
        void operator()(Tox_Options *options) {
            tox_options_free(options);
        }
    };

    std::unique_ptr<Tox_Options, Tox_Options_Deleter> opts(tox_options_new(nullptr));
    if (!opts) {
        throw_tox_exception(env, "New", "MALLOC");
        return 0;
    }

    opts->ipv6_enabled = ipv6Enabled;
    opts->udp_enabled = udpEnabled;

    opts->proxy_type = (TOX_PROXY_TYPE) proxyType;
    UTFChars proxy_address(env, proxyAddress);
    opts->proxy_address = proxy_address.data();
    opts->proxy_port = proxyPort;

    return with_error_handling(env, "New", [](TOX_ERR_NEW error) {
        switch (error) {
            success_case(NEW);
            failure_case(NEW, NULL);
            failure_case(NEW, MALLOC);
            failure_case(NEW, PORT_ALLOC);
            failure_case(NEW, PROXY_BAD_HOST);
            failure_case(NEW, PROXY_BAD_PORT);
            failure_case(NEW, PROXY_NOT_FOUND);
        }
        return unhandled();
    }, [env](Tox *tox_pointer) {
        std::unique_ptr<Tox, ToxDeleter> tox(tox_pointer);
        assert(tox != nullptr);

        // Create the master events object.
        std::unique_ptr<ToxEvents> events(new ToxEvents);

        // Set up our callbacks.
        tox_callback_connection_status       (tox.get(), tox4j_connection_status_cb,        events.get());
        tox_callback_friend_name             (tox.get(), tox4j_friend_name_cb,              events.get());
        tox_callback_friend_status_message   (tox.get(), tox4j_friend_status_message_cb,    events.get());
        tox_callback_friend_status           (tox.get(), tox4j_friend_status_cb,            events.get());
        tox_callback_friend_connection_status(tox.get(), tox4j_friend_connection_status_cb, events.get());
        tox_callback_friend_typing           (tox.get(), tox4j_friend_typing_cb,            events.get());
        tox_callback_read_receipt            (tox.get(), tox4j_read_receipt_cb,             events.get());
        tox_callback_friend_request          (tox.get(), tox4j_friend_request_cb,           events.get());
        tox_callback_friend_message          (tox.get(), tox4j_friend_message_cb,           events.get());
        tox_callback_friend_action           (tox.get(), tox4j_friend_action_cb,            events.get());
        tox_callback_file_control            (tox.get(), tox4j_file_control_cb,             events.get());
        tox_callback_file_request_chunk      (tox.get(), tox4j_file_request_chunk_cb,       events.get());
        tox_callback_file_receive            (tox.get(), tox4j_file_receive_cb,             events.get());
        tox_callback_file_receive_chunk      (tox.get(), tox4j_file_receive_chunk_cb,       events.get());
        tox_callback_friend_lossy_packet     (tox.get(), tox4j_friend_lossy_packet_cb,      events.get());
        tox_callback_friend_lossless_packet  (tox.get(), tox4j_friend_lossless_packet_cb,   events.get());

        // We can create the new instance outside InstanceManager' critical section.
        ToxInstance instance {
            std::move(tox),
            std::move(events),
            std::unique_ptr<std::mutex>(new std::mutex)
        };

        // This lock guards the instance manager.
        std::lock_guard<std::mutex> lock(InstanceManager::self.mutex);
        return InstanceManager::self.add(std::move(instance));
    }, tox_new, opts.get());
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    toxKill
 * Signature: (I)I
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxCoreImpl_toxKill
  (JNIEnv *env, jclass, jint instanceNumber)
{
    std::lock_guard<std::mutex> lock(InstanceManager::self.mutex);

    if (instanceNumber < 0) {
        throw_illegal_state_exception(env, instanceNumber, "Tox instance out of range");
        return;
    }

    if (!InstanceManager::self.isValid(instanceNumber)) {
        throw_tox_killed_exception(env, instanceNumber, "close called on invalid instance");
        return;
    }

    // After this move, the pointers in instance_vector[instance_number] will all be nullptr...
    ToxInstance dying(InstanceManager::self.remove(instanceNumber));

    // ... so that this check will fail, if the function is called twice on the same instance.
    if (!dying.tox) {
        throw_tox_killed_exception(env, instanceNumber, "close called on already closed instance");
        return;
    }

    assert(dying.isLive());
    std::lock_guard<std::mutex> ilock(*dying.mutex);
}

/*
 * Class:     im_tox_tox4jToxCoreImpl
 * Method:    finalize
 * Signature: (I)V
 */
JNIEXPORT void JNICALL Java_im_tox_tox4j_ToxCoreImpl_finalize
  (JNIEnv *env, jclass, jint instanceNumber)
{
    if (instanceNumber == 0) {
        // This can happen when an exception is thrown from the constructor, giving this object an invalid state,
        // containing instanceNumber = 0.
        return;
    }

    if (InstanceManager::self.empty()) {
        throw_illegal_state_exception(env, instanceNumber, "Tox instance manager is empty");
        return;
    }

    std::lock_guard<std::mutex> lock(InstanceManager::self.mutex);
    if (!InstanceManager::self.isValid(instanceNumber)) {
        throw_illegal_state_exception(env, instanceNumber,
            "Tox instance out of range (max: " + to_string(InstanceManager::self.size() - 1) + ")");
        return;
    }

    // An instance should never be on this list twice.
    if (InstanceManager::self.isFree(instanceNumber)) {
        throw_illegal_state_exception(env, instanceNumber, "Tox instance already on free list");
        return;
    }

    // This instance was leaked, kill it before setting it free.
    if (InstanceManager::self[instanceNumber].isLive()) {
        ToxInstance dying(InstanceManager::self.remove(instanceNumber));
        assert(dying.isLive());
        std::lock_guard<std::mutex> ilock(*dying.mutex);
    }

    assert(InstanceManager::self[instanceNumber].isDead());
    InstanceManager::self.setFree(instanceNumber);
}
