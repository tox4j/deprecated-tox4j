#include <tox/core.h>

void tox_options_default(struct Tox_Options *options) { }
struct Tox_Options *tox_options_new(TOX_ERR_OPTIONS_NEW *error) { return 0; }
void tox_options_free(struct Tox_Options *options) { }
Tox *tox_new(struct Tox_Options const *options, TOX_ERR_NEW *error) { *error = TOX_ERR_NEW_MALLOC; return 0; }
void tox_kill(Tox *tox) { }
size_t tox_save_size(Tox const *tox) { return 0; }
void tox_save(Tox const *tox, uint8_t *data) { }
bool tox_load(Tox *tox, uint8_t const *data, size_t length, TOX_ERR_LOAD *error) { return 0; }
bool tox_bootstrap(Tox *tox, char const *address, uint16_t port, uint8_t const *public_key, TOX_ERR_BOOTSTRAP *error) { return 0; }
bool tox_is_connected(Tox const *tox) { return 0; }
void tox_callback_connection_status(Tox *tox, tox_connection_status_cb *function, void *user_data) { }
uint16_t tox_get_port(Tox const *tox, TOX_ERR_GET_PORT *error) { return 0; }
uint32_t tox_iteration_time(Tox const *tox) { return 0; }
void tox_iteration(Tox *tox) { }
void tox_get_self_address(Tox const *tox, uint8_t *address) { }
void tox_set_nospam(Tox *tox, uint32_t nospam) { }
uint32_t tox_get_nospam(Tox const *tox) { return 0; }
void tox_get_self_client_id(Tox const *tox, uint8_t *client_id) { }
void tox_get_secret_key(Tox const *tox, uint8_t *secret_key) { }
bool tox_set_self_name(Tox *tox, uint8_t const *name, size_t length, TOX_ERR_SET_INFO *error) { return 0; }
size_t tox_self_name_size(Tox const *tox) { return 0; }
void tox_get_self_name(Tox const *tox, uint8_t *name) { }
bool tox_set_self_status_message(Tox *tox, uint8_t const *status, size_t length, TOX_ERR_SET_INFO *error) { return 0; }
size_t tox_self_status_message_size(Tox const *tox) { return 0; }
void tox_get_self_status_message(Tox const *tox, uint8_t *status) { }
void tox_set_self_status(Tox *tox, TOX_STATUS user_status) { }
TOX_STATUS tox_get_self_status(Tox const *tox) { return 0; }
uint32_t tox_add_friend(Tox *tox, uint8_t const *address, uint8_t const *message, size_t length, TOX_ERR_ADD_FRIEND *error) { return 0; }
uint32_t tox_add_friend_norequest(Tox *tox, uint8_t const *client_id, TOX_ERR_ADD_FRIEND *error) { return 0; }
bool tox_delete_friend(Tox *tox, uint32_t friend_number, TOX_ERR_DELETE_FRIEND *error) { return 0; }
uint32_t tox_get_friend_number(Tox const *tox, uint8_t const *client_id, TOX_ERR_GET_FRIEND_NUMBER *error) { return 0; }
bool tox_get_friend_client_id(Tox const *tox, uint32_t friend_number, uint8_t *client_id, TOX_ERR_GET_CLIENT_ID *error) { return 0; }
bool tox_friend_exists(Tox const *tox, uint32_t friend_number) { return 0; }
size_t tox_friend_list_size(Tox const *tox) { return 0; }
void tox_get_friend_list(Tox const *tox, uint32_t *list) { }
size_t tox_get_friend_name_size(Tox const *tox, uint32_t friend_number, TOX_ERR_FRIEND_QUERY *error) { return 0; }
bool tox_get_friend_name(Tox const *tox, uint32_t friend_number, uint8_t *name, TOX_ERR_FRIEND_QUERY *error) { return 0; }
void tox_callback_friend_name(Tox *tox, tox_friend_name_cb *function, void *user_data) { }
size_t tox_get_friend_status_message_size(Tox const *tox, uint32_t friend_number, TOX_ERR_FRIEND_QUERY *error) { return 0; }
bool tox_get_friend_status_message(Tox const *tox, uint32_t friend_number, uint8_t *message, TOX_ERR_FRIEND_QUERY *error) { return 0; }
void tox_callback_friend_status_message(Tox *tox, tox_friend_status_message_cb *function, void *user_data) { }
TOX_STATUS tox_get_friend_status(Tox const *tox, uint32_t friend_number, TOX_ERR_FRIEND_QUERY *error) { return 0; }
void tox_callback_friend_status(Tox *tox, tox_friend_status_cb *function, void *user_data) { }
bool tox_get_friend_is_connected(Tox const *tox, uint32_t friend_number, TOX_ERR_FRIEND_QUERY *error) { return 0; }
void tox_callback_friend_connected(Tox *tox, tox_friend_connected_cb *function, void *user_data) { }
bool tox_get_friend_is_typing(Tox const *tox, uint32_t friend_number, TOX_ERR_FRIEND_QUERY *error) { return 0; }
void tox_callback_friend_typing(Tox *tox, tox_friend_typing_cb *function, void *user_data) { }
bool tox_set_typing(Tox *tox, uint32_t friend_number, bool is_typing, TOX_ERR_SET_TYPING *error) { return 0; }
uint32_t tox_send_message(Tox *tox, uint32_t friend_number, uint8_t const *message, size_t length, TOX_ERR_SEND_MESSAGE *error) { return 0; }
uint32_t tox_send_action(Tox *tox, uint32_t friend_number, uint8_t const *action, size_t length, TOX_ERR_SEND_MESSAGE *error) { return 0; }
void tox_callback_read_receipt(Tox *tox, tox_read_receipt_cb *function, void *user_data) { }
void tox_callback_friend_request(Tox *tox, tox_friend_request_cb *function, void *user_data) { }
void tox_callback_friend_message(Tox *tox, tox_friend_message_cb *function, void *user_data) { }
void tox_callback_friend_action(Tox *tox, tox_friend_action_cb *function, void *user_data) { }
bool tox_hash(uint8_t *hash, uint8_t const *data, size_t length) { return 0; }
bool tox_file_control(Tox *tox, uint32_t friend_number, uint8_t file_number, TOX_FILE_CONTROL control, TOX_ERR_FILE_CONTROL *error) { return 0; }
void tox_callback_file_control(Tox *tox, tox_file_control_cb *function, void *user_data) { }
uint8_t tox_file_send(Tox *tox, uint32_t friend_number, TOX_FILE_KIND kind, uint64_t file_size, uint8_t const *filename, size_t filename_length, TOX_ERR_SEND_FILE *error) { return 0; }
void tox_callback_file_send_chunk(Tox *tox, tox_file_send_chunk_cb *function, void *user_data) { }
void tox_callback_file_recv(Tox *tox, tox_file_recv_cb *function, void *user_data) { }
void tox_callback_file_recv_chunk(Tox *tox, tox_file_recv_chunk_cb *function, void *user_data) { }
bool tox_send_lossy_packet(Tox *tox, uint32_t friend_number, uint8_t const *data, size_t length, TOX_ERR_SEND_CUSTOM_PACKET *error) { return 0; }
void tox_callback_lossy_packet(Tox *tox, tox_lossy_packet_cb *function, void *user_data) { }
bool tox_send_lossless_packet(Tox *tox, uint32_t friend_number, uint8_t const *data, size_t length, TOX_ERR_SEND_CUSTOM_PACKET *error) { return 0; }
void tox_callback_lossless_packet(Tox *tox, tox_lossy_packet_cb *function, void *user_data) { }
