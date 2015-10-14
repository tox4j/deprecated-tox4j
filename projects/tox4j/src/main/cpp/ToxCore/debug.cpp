#include "ToxCore.h"

#ifdef TOX_VERSION_MAJOR

#include <algorithm>
#include <vector>

register_funcs (
  register_func (tox_version_major),
  register_func (tox_version_minor),
  register_func (tox_version_patch),
  register_func (tox_version_is_compatible),
  register_func (tox_options_default),
  register_func (tox_options_free),
  register_func (tox_new),
  register_func (tox_kill),
  register_func (tox_get_savedata_size),
  register_func (tox_get_savedata),
  register_func (tox_bootstrap),
  register_func (tox_add_tcp_relay),
  register_func (tox_self_get_connection_status),
  register_func (tox_callback_self_connection_status),
  register_func (tox_iteration_interval),
  register_func (tox_iterate),
  register_func (tox_self_get_address),
  register_func (tox_self_set_nospam),
  register_func (tox_self_get_nospam),
  register_func (tox_self_get_public_key),
  register_func (tox_self_get_secret_key),
  register_func (tox_self_set_name),
  register_func (tox_self_get_name_size),
  register_func (tox_self_get_name),
  register_func (tox_self_set_status_message),
  register_func (tox_self_get_status_message_size),
  register_func (tox_self_get_status_message),
  register_func (tox_self_set_status),
  register_func (tox_self_get_status),
  register_func (tox_friend_add),
  register_func (tox_friend_add_norequest),
  register_func (tox_friend_delete),
  register_func (tox_friend_by_public_key),
  register_func (tox_friend_exists),
  register_func (tox_self_get_friend_list_size),
  register_func (tox_self_get_friend_list),
  register_func (tox_friend_get_public_key),
  register_func (tox_friend_get_last_online),
  register_func (tox_friend_get_name_size),
  register_func (tox_friend_get_name),
  register_func (tox_callback_friend_name),
  register_func (tox_friend_get_status_message_size),
  register_func (tox_friend_get_status_message),
  register_func (tox_callback_friend_status_message),
  register_func (tox_friend_get_status),
  register_func (tox_callback_friend_status),
  register_func (tox_friend_get_connection_status),
  register_func (tox_callback_friend_connection_status),
  register_func (tox_friend_get_typing),
  register_func (tox_callback_friend_typing),
  register_func (tox_self_set_typing),
  register_func (tox_friend_send_message),
  register_func (tox_callback_friend_read_receipt),
  register_func (tox_callback_friend_request),
  register_func (tox_callback_friend_message),
  register_func (tox_hash),
  register_func (tox_file_control),
  register_func (tox_callback_file_recv_control),
  register_func (tox_file_seek),
  register_func (tox_file_get_file_id),
  register_func (tox_file_send),
  register_func (tox_file_send_chunk),
  register_func (tox_callback_file_chunk_request),
  register_func (tox_callback_file_recv),
  register_func (tox_callback_file_recv_chunk),
  register_func (tox_friend_send_lossy_packet),
  register_func (tox_friend_send_lossless_packet),
  register_func (tox_callback_friend_lossy_packet),
  register_func (tox_callback_friend_lossless_packet),
  register_func (tox_self_get_dht_id),
  register_func (tox_self_get_udp_port),
  register_func (tox_self_get_tcp_port)
);

template<>
void
print_arg<Tox *> (Tox *tox)
{
  static std::vector<Tox *> ids;
  auto found = std::find (ids.begin (), ids.end (), tox);
  if (found == ids.end ())
    {
      ids.push_back (tox);
      found = ids.end () - 1;
    }
  debug_out << "@" << (found - ids.begin () + 1);
}

template<>
void
print_arg<Tox_Options *> (Tox_Options *options)
{
  (void)options;
  debug_out << "<Tox_Options>";
}

#define enum_case(ENUM)                               \
    case TOX_##ENUM: debug_out << "TOX_" #ENUM; break

template<>
void
print_arg<TOX_FILE_KIND> (TOX_FILE_KIND kind)
{
  switch (kind)
    {
    enum_case (FILE_KIND_DATA);
    enum_case (FILE_KIND_AVATAR);
    default:
      debug_out << "(TOX_FILE_KIND)" << kind;
      break;
    }
}

#endif
