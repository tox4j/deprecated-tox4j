package im.tox.tox4j.av.callbacks

import im.tox.tox4j.annotations.{ NotNull, Nullable }

/**
 * Triggered when a video frame is received.
 */
trait VideoReceiveFrameCallback[ToxCoreState] {
  /**
   * @param friendNumber The friend number of the friend who sent a video frame.
   * @param width Width of the frame in pixels.
   * @param height Height of the frame in pixels.
   * @param y Y-plane.
   * @param u U-plane.
   * @param v V-plane.
   * @param a Alpha-plane.
   *          The size of plane data is derived from width and height where
   *          Y = max(width,   abs(yStride)) * height
   *          U = max(width/2, abs(uStride)) * (height/2)
   *          V = max(width/2, abs(vStride)) * (height/2)
   *          A = max(width,   abs(aStride)) * height.
   * @param yStride Stride length for Y-plane.
   * @param uStride Stride length for U-plane.
   * @param vStride Stride length for V-plane.
   * @param aStride Stride length for Alpha-plane. Strides represent padding for
   *                each plane that may or may not be present. You must handle
   *                strides in your image processing code. Strides are negative
   *                if the image is bottom-up hence why you MUST abs() it when
   *                calculating plane buffer size.
   */
  // scalastyle:ignore parameter.number
  def receiveVideoFrame(
    friendNumber: Int,
    width: Int, height: Int,
    @NotNull y: Array[Byte], @NotNull u: Array[Byte], @NotNull v: Array[Byte], @Nullable a: Array[Byte],
    yStride: Int, uStride: Int, vStride: Int, aStride: Int
  )(state: ToxCoreState): ToxCoreState = state
}
