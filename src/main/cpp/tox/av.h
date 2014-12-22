#pragma once

#include <stdbool.h>
#include <stdint.h>

#include <tox/av_compat.h>

/** \page av Public audio/video API for Tox clients.
 *
 * Unlike the Core API, this API is fully thread-safe. The library will ensure
 * the proper synchronisation of parallel calls.
 */


/**
 * The type of the Tox Audio/Video subsystem object.
 */
typedef struct ToxAV ToxAV;


#ifndef TOX_DEFINED
#define TOX_DEFINED
/**
 * The type of a Tox instance. Repeated here so this file does not have a direct
 * dependency on the Core interface.
 */
typedef struct Tox Tox;
#endif


/*******************************************************************************
 *
 * :: Creation and destruction
 *
 ******************************************************************************/


typedef enum TOXAV_ERR_NEW {
  TOXAV_ERR_NEW_OK,
  TOXAV_ERR_NEW_NULL,
  /**
   * Memory allocation failure while trying to allocate structures required for
   * the A/V session.
   */
  TOXAV_ERR_NEW_MALLOC,
  /**
   * Attempted to create a second session for the same Tox instance.
   */
  TOXAV_ERR_NEW_MULTIPLE,
  /**
   * Initialisation of audio or video codecs failed.
   */
  TOXAV_ERR_NEW_CODECS
} TOXAV_ERR_NEW;

/**
 * Start new A/V session. There can only be only one session per Tox instance.
 */
ToxAV *toxav_new(Tox *tox, TOXAV_ERR_NEW *error);


/**
 * Releases all resources associated with the A/V session.
 *
 * If any calls were ongoing, these will be forcibly terminated without
 * notifying peers. After calling this function, no other functions may be
 * called and the av pointer becomes invalid.
 */
void toxav_kill(ToxAV *av);


/**
 * Returns the Tox instance the A/V object was created for.
 */
Tox *toxav_get_tox(ToxAV *av);


/*******************************************************************************
 *
 * :: A/V event loop
 *
 ******************************************************************************/


/**
 * Returns the interval in milliseconds when the next toxav_iteration should be
 * called. If no call is active at the moment, this function returns 200.
 */
uint32_t toxav_iteration_interval(ToxAV const *av);


/**
 * Main loop for the session. This function needs to be called in intervals of
 * toxav_iteration_interval() milliseconds. It is best called in the same loop
 * as tox_iteration.
 */
void toxav_iteration(ToxAV *av);


/*******************************************************************************
 *
 * :: Call setup
 *
 ******************************************************************************/


/**
 * Encoding settings.
 */
struct ToxAV_Options {
  /**
   * Enable audio call. If this is set to false, all audio settings are ignored.
   */
  bool audio_enabled;
  /**
   * Enable video call. If this is set to false, all video settings are ignored.
   */
  bool video_enabled;

  /**
   * Bit rate of video transmission in Kb/sec. TODO: range?
   */
  uint32_t video_bit_rate;
  /**
   * Maximum width (x) of a video image in pixels. TODO: range?
   */
  uint16_t max_video_width;
  /**
   * Maximum height (y) of a video image in pixels. TODO: range?
   */
  uint16_t max_video_height;

  /**
   * Bit rate of audio transmission in b/sec. This value can range from 500 to
   * 512000. TODO: anything between these, or are there intervals?
   */
  uint32_t audio_bit_rate;
  /**
   * Number of samples of audio per second [Hz]. Valid sampling rates are 8000,
   * 12000, 16000, 24000, and 48000.
   */
  uint32_t audio_sampling_rate;
  /**
   * Number of milliseconds per audio frame. A higher number here means better
   * compression, but higher latency. Valid durations are 10, 20, 40, and 60ms. TODO: correct?
   */
  uint16_t audio_frame_duration;
  /**
   * Number of channels in audio transmission. Currently, only mono (1) and
   * stereo (2) are supported.
   */
  uint16_t audio_channels;
};


/**
 * Initialises a ToxAV_Options object with the default options.
 *
 * The result of this function is independent of the original options. All
 * values will be overwritten, no values will be read (so it is permissible
 * to pass an uninitialised object).
 *
 * If options is NULL, this function has no effect.
 *
 * @param options An options object to be filled with default options.
 */
void toxav_options_default(struct ToxAV_Options *options);


typedef enum TOXAV_ERR_OPTIONS_NEW {
  TOXAV_ERR_OPTIONS_NEW_OK,
  /**
   * The function failed to allocate enough memory for the options struct.
   */
  TOXAV_ERR_OPTIONS_NEW_MALLOC
} TOXAV_ERR_OPTIONS_NEW;

/**
 * Allocates a new Tox_Options object and initialises it with the default
 * options. This function can be used to preserve long term ABI compatibility by
 * giving the responsibility of allocation and deallocation to the Tox library.
 *
 * Objects returned from this function must be freed using the tox_options_free
 * function.
 *
 * @return A new Tox_Options object with default options or NULL on failure.
 */
struct ToxAV_Options *toxav_options_new(TOXAV_ERR_OPTIONS_NEW *error);


/**
 * Releases all resources associated with an options objects.
 *
 * Passing a pointer that was not returned by toxav_options_new results in
 * undefined behaviour.
 */
void toxav_options_free(struct ToxAV_Options *options);


typedef enum TOXAV_ERR_OPTIONS {
  /**
   * All the options seem to be correct. This is not a guarantee that a call
   * will succeed, but it's a strong indication that the client was set up
   * correctly.
   */
  TOXAV_ERR_OPTIONS_OK,
  /**
   * The video resolution is invalid. TODO: range?
   */
  TOXAV_ERR_OPTIONS_RESOLUTION,
  /**
   * The audio bitrate is invalid. TODO: range?
   */
  TOXAV_ERR_OPTIONS_AUDIO_BITRATE,
  /**
   * The video bitrate is invalid. TODO: range?
   */
  TOXAV_ERR_OPTIONS_VIDEO_BITRATE,
  /**
   * Audio was enabled, but audio_channels was invalid. An audio call requires
   * at least one channel and can have at most 2 channels.
   */
  TOXAV_ERR_OPTIONS_AUDIO_CHANNELS
} TOXAV_ERR_OPTIONS;

/**
 * Find out whether the options are valid and if not, what is wrong with them.
 */
TOXAV_ERR_OPTIONS toxav_analyse_options(ToxAV *av, struct ToxAV_Options const *options);



typedef enum TOXAV_ERR_CALL {
  TOXAV_ERR_CALL_OK,
  /**
   * A resource allocation error occurred while trying to create the structures
   * required for the call.
   */
  TOXAV_ERR_CALL_MALLOC,
  /**
   * The friend number did not designate a valid friend.
   */
  TOXAV_ERR_CALL_FRIEND_NOT_FOUND,
  /**
   * The friend was valid, but not currently connected.
   */
  TOXAV_ERR_CALL_FRIEND_NOT_CONNECTED,
  /**
   * Some options were invalid. Call toxav_analyse_options to find out why.
   */
  TOXAV_ERR_CALL_INVALID_OPTIONS,
  /**
   * Attempted to call a friend while already in an audio or video call with
   * them.
   */
  TOXAV_ERR_CALL_ALREADY_IN_CALL
} TOXAV_ERR_CALL;

/**
 * Call a friend. This will start ringing the friend.
 *
 * It is the client's responsibility to stop ringing after a certain timeout,
 * if such behaviour is desired. If the client does not stop ringing, the A/V
 * library will not stop until the friend is disconnected.
 *
 * @param friend_number The friend number of the friend that should be called.
 * @param options An options object that defines call parameters.
 */
bool toxav_call(ToxAV *av, uint32_t friend_number, struct ToxAV_Options const *options, TOXAV_ERR_CALL *error);


/**
 * The function type for the `call` callback.
 */
typedef void toxav_call_cb(ToxAV *av, uint32_t friend_number, struct ToxAV_Options const *options, void *user_data);

/**
 * Set the callback for the `call` event. Pass NULL to unset.
 *
 * This event is triggered when a call is received from a friend.
 */
void toxav_callback_call(ToxAV *av, toxav_call_cb *function, void *user_data);


typedef enum TOXAV_ERR_ANSWER {
  TOXAV_ERR_ANSWER_OK,
  /**
   * A resource allocation error occurred while trying to create the structures
   * required for the call.
   */
  TOXAV_ERR_ANSWER_MALLOC,
  /**
   * The friend number did not designate a valid friend.
   */
  TOXAV_ERR_ANSWER_FRIEND_NOT_FOUND,
  /**
   * The friend was valid, but they are not currently trying to initiate a call.
   * This is also returned if this client is already in a call with the friend.
   */
  TOXAV_ERR_ANSWER_FRIEND_NOT_CALLING,
  /**
   * Some options were invalid. Call toxav_analyse_options to find out why.
   */
  TOXAV_ERR_ANSWER_INVALID_OPTIONS
} TOXAV_ERR_ANSWER;

/**
 * Accept an incoming call.
 *
 * If an allocation error occurs while answering a call, both participants will
 * receive TOXAV_CALL_CONTROL_ERROR and the call will end.
 *
 * @param friend_number The friend number of the friend that should be called.
 * @param options The A/V settings that the client will use.
 */
bool toxav_answer(ToxAV *av, uint32_t friend_number, struct ToxAV_Options const *options, TOXAV_ERR_ANSWER *error);


/*******************************************************************************
 *
 * :: Call control
 *
 ******************************************************************************/


typedef enum TOXAV_CALL_CONTROL {
  /**
   * Resume a previously paused call. Only valid if the pause was caused by this
   * client. Not valid before the call is accepted.
   */
  TOXAV_CALL_CONTROL_RESUME,
  /**
   * Put a call on hold. Not valid before the call is accepted.
   */
  TOXAV_CALL_CONTROL_PAUSE,
  /**
   * Reject a call if it was not answered, yet. Cancel a call after it was
   * answered.
   */
  TOXAV_CALL_CONTROL_CANCEL,
  /**
   * [System] Sent by the AV core if an error occurred. This is never sent
   * explicitly by clients. If a client tries to send this code itself, it
   * receives the error code DENIED.
   */
  TOXAV_CALL_CONTROL_ERROR,
} TOXAV_CALL_CONTROL;


typedef enum TOXAV_ERR_CALL_CONTROL {
  TOXAV_ERR_CALL_CONTROL_OK,
  /**
   * The friend_number passed did not designate a valid friend.
   */
  TOXAV_ERR_CALL_CONTROL_FRIEND_NOT_FOUND,
  /**
   * This client is currently not in a call with the friend. Before the call is
   * answered, only CANCEL is a valid control.
   */
  TOXAV_ERR_CALL_CONTROL_FRIEND_NOT_IN_CALL,
  /**
   * Attempted to resume a call that was not paused.
   */
  TOXAV_ERR_CALL_CONTROL_NOT_PAUSED,
  /**
   * Attempted to resume a call that was paused by the other party. Also set if
   * the client attempted to send a system-only control.
   */
  TOXAV_ERR_CALL_CONTROL_DENIED,
  /**
   * The call was already paused on this client. It is valid to pause if the
   * other party paused the call. The call will resume after both parties sent
   * the RESUME control.
   */
  TOXAV_ERR_CALL_CONTROL_ALREADY_PAUSED
} TOXAV_ERR_CALL_CONTROL;

/**
 * Sends a call control command to a friend.
 *
 * @param friend_number The friend number of the friend this client is in a call
 *   with.
 * @param control The control command to send.
 *
 * @return true on success.
 */
bool toxav_call_control(ToxAV *av, uint32_t friend_number, TOXAV_CALL_CONTROL control, TOXAV_ERR_CALL_CONTROL *error);


/**
 * The function type for the `call_control` callback.
 *
 * @param friend_number The friend number of the friend who sent the control.
 * @param control The call control command received.
 */
typedef void toxav_call_control_cb(ToxAV *av, uint32_t friend_number, TOXAV_CALL_CONTROL control, void *user_data);

/**
 * Set the callback for the `call_control` event. Pass NULL to unset.
 *
 * This event is triggered when a call control command is received from a
 * friend.
 */
void toxav_callback_call_control(ToxAV *av, toxav_call_control_cb *function, void *user_data);


typedef enum TOXAV_ERR_CHANGE_OPTIONS {
  TOXAV_ERR_CHANGE_OPTIONS_OK,
  /**
   * The friend_number passed did not designate a valid friend.
   */
  TOXAV_ERR_CHANGE_OPTIONS_FRIEND_NOT_FOUND,
  /**
   * This client is currently not in a call with the friend. Before the call is
   * answered, only CANCEL is a valid control.
   */
  TOXAV_ERR_CHANGE_OPTIONS_FRIEND_NOT_IN_CALL,
  /**
   * Some options were invalid. Call toxav_analyse_options to find out why.
   */
  TOXAV_ERR_CHANGE_OPTIONS_INVALID_OPTIONS
} TOXAV_ERR_CHANGE_OPTIONS;

/**
 * Change A/V call options. Can be used to start/stop audio and video, or change
 * the encoding settings.
 *
 * @param friend_number The friend number of the friend the options should
 *   change for.
 * @param options The new A/V call options.
 */
bool toxav_change_options(ToxAV *av, uint32_t friend_number, struct ToxAV_Options const *options, TOXAV_ERR_CHANGE_OPTIONS *error);


/**
 * The function type for the `change_options` callback.
 *
 * This change does not necessarily mean that the receiving client also needs to
 * change their settings. E.g. a friend may decide to stop sending video, but
 * can still receive it.
 *
 * @param friend_number The friend number of the friend who changed their call
 *   options.
 * @param control The call control command received.
 */
typedef void toxav_change_options_cb(ToxAV *av, uint32_t friend_number, struct ToxAV_Options const *options, void *user_data);

/**
 * Set the callback for the `change_options` event. Pass NULL to unset.
 *
 * This event is triggered when a friend changed their own options.
 */
void toxav_callback_change_options(ToxAV *av, toxav_change_options_cb *function, void *user_data);


typedef enum TOXAV_ERR_FRIEND_GET_OPTIONS {
  TOXAV_ERR_FRIEND_GET_OPTIONS_OK,
  /**
   * The options pointer was NULL.
   */
  TOXAV_ERR_FRIEND_GET_OPTIONS_NULL,
  /**
   * The friend_number did not designate a valid friend.
   */
  TOXAV_ERR_FRIEND_GET_OPTIONS_FRIEND_NOT_FOUND
} TOXAV_ERR_FRIEND_GET_OPTIONS;

/**
 * Get A/V call options from the friend.
 *
 * These are the latest available options from the friend, received through
 * either the 'call' or the 'change_options' event.
 *
 * @param friend_number The friend number of the friend for which to get the
 *   current call options.
 * @param options A pointer to an options object. Does not need to be
 *   initialised with any values.
 */
bool toxav_friend_get_options(ToxAV *av, uint32_t friend_number, struct ToxAV_Options *options, TOXAV_ERR_FRIEND_GET_OPTIONS *error);


/*******************************************************************************
 *
 * :: A/V sending
 *
 ******************************************************************************/


/**
 * Common error codes for the send_*_frame functions.
 */
typedef enum TOXAV_ERR_SEND_FRAME {
  TOXAV_ERR_SEND_FRAME_OK,
  /**
   * In case of video, one of Y, U, or V was NULL. In case of audio, the samples
   * data pointer was NULL.
   */
  TOXAV_ERR_SEND_FRAME_NULL,
  /**
   * The friend_number passed did not designate a valid friend.
   */
  TOXAV_ERR_SEND_FRAME_FRIEND_NOT_FOUND,
  /**
   * This client is currently not in a call with the friend.
   */
  TOXAV_ERR_SEND_FRAME_FRIEND_NOT_IN_CALL,
  /**
   * No video frame had been requested through the `request_video_frame` event,
   * but the client tried to send one, anyway.
   */
  TOXAV_ERR_SEND_FRAME_NOT_REQUESTED
} TOXAV_ERR_SEND_FRAME;


/**
 * The function type for the `request_video_frame` callback.
 *
 * @param friend_number The friend number of the friend for which the next video
 *   frame should be sent.
 */
typedef void toxav_request_video_frame_cb(ToxAV *av, uint32_t friend_number, void *user_data);

/**
 * Set the callback for the `request_video_frame` event. Pass NULL to unset.
 */
void toxav_callback_request_video_frame(ToxAV *av, toxav_request_video_frame_cb *function, void *user_data);


/**
 * Send a video frame to a friend.
 *
 * This is called in response to receiving the `request_video_frame` event.
 *
 * Each plane should contain (width * height) pixels as set in the options. The
 * Alpha plane can be NULL, in which case every pixel is assumed fully opaque.
 *
 * @param friend_number The friend number of the friend to which to send a video
 *   frame.
 * @param y Y (Luminance) plane data.
 * @param u U (Chroma) plane data.
 * @param v V (Chroma) plane data.
 * @param a A (Alpha) plane data.
 */
bool toxav_send_video_frame(ToxAV *av, uint32_t friend_number, uint8_t const *y, uint8_t const *u, uint8_t const *v, uint8_t const *a, TOXAV_ERR_SEND_FRAME *error);


/**
 * The function type for the `request_audio_frame` callback.
 *
 * @param friend_number The friend number of the friend for which the next audio
 *   frame should be sent.
 * @param size The number of audio samples to send in the next packet.
 */
typedef void toxav_request_audio_frame_cb(ToxAV *av, uint32_t friend_number, uint16_t size, void *user_data);

/**
 * Set the callback for the `request_audio_frame` event. Pass NULL to unset.
 */
void toxav_callback_request_audio_frame(ToxAV *av, toxav_request_audio_frame_cb *function, void *user_data);


/**
 * Send an audio frame to a friend.
 *
 * This is called in response to receiving the `request_audio_frame` event.
 *
 * @param friend_number The friend number of the friend to which to send an
 *   audio frame.
 * @param samples An array of audio samples. The number of samples sent must be
 *   equal to the size parameter of the last request_video_frame callback.
 */
bool toxav_send_audio_frame(ToxAV *av, uint32_t friend_number, uint16_t const *samples, TOXAV_ERR_SEND_FRAME *error);



/*******************************************************************************
 *
 * :: A/V receiving
 *
 ******************************************************************************/


/**
 * The function type for the `receive_video_frame` callback.
 *
 * Each plane contains (width * height) pixels as received through the `call` or
 * `change_options` event. The Alpha plane can be NULL, in which case every
 * pixel should be assumed fully opaque.
 *
 * @param friend_number The friend number of the friend who sent a video frame.
 * @param y Y (Luminance) plane data.
 * @param u U (Chroma) plane data.
 * @param v V (Chroma) plane data.
 * @param a A (Alpha) plane data.
 */
typedef void toxav_receive_video_frame_cb(ToxAV *av, uint32_t friend_number, uint8_t const *y, uint8_t const *u, uint8_t const *v, uint8_t const *a, void *user_data);

/**
 * Set the callback for the `receive_video_frame` event. Pass NULL to unset.
 */
void toxav_callback_receive_video_frame(ToxAV *av, toxav_receive_video_frame_cb *function, void *user_data);


/**
 * The function type for the `receive_audio_frame` callback.
 *
 * @param friend_number The friend number of the friend who sent an audio frame.
 * @param samples An array of audio samples.
 * @param size The number of audio samples received.
 */
typedef void toxav_receive_audio_frame_cb(ToxAV *av, uint32_t friend_number, uint16_t const *samples, uint16_t size, void *user_data);

/**
 * Set the callback for the `receive_audio_frame` event. Pass NULL to unset.
 */
void toxav_callback_receive_audio_frame(ToxAV *av, toxav_receive_audio_frame_cb *function, void *user_data);
