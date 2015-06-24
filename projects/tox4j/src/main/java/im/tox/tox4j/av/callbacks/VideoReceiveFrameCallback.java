package im.tox.tox4j.av.callbacks;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.annotations.Nullable;

/**
 * Triggered when a video frame is received.
 */
public interface VideoReceiveFrameCallback {
  VideoReceiveFrameCallback IGNORE = new VideoReceiveFrameCallback() {
    @SuppressWarnings("checkstyle:parametername")
    @Override
    public void receiveVideoFrame(
        int friendNumber,
        int width, int height,
        @NotNull byte[] y, @NotNull byte[] u, @NotNull byte[] v, @Nullable byte[] a,
        int yStride, int uStride, int vStride, int aStride
    ) {
    }
  };

  /**
   * @param friendNumber The friend number of the friend who sent a video frame.
   * @param width Width of the frame in pixels.
   * @param height Height of the frame in pixels.
   * @param y Y-plane.
   * @param u U-plane.
   * @param v V-plane.
   * @param a Alpha-plane.
   *          The size of plane data is derived from width and height where
   *          Y = MAX(width, abs(ystride)) * height,
   *          U = MAX(width/2, abs(ustride)) * (height/2) and
   *          V = MAX(width/2, abs(vstride)) * (height/2).
   *          A = MAX(width, abs(astride)) * height.
   * @param yStride Stride length for Y-plane.
   * @param uStride Stride length for U-plane.
   * @param vStride Stride length for V-plane.
   * @param aStride Stride length for Alpha-plane. Strides represent padding for
   *                each plane that may or may not be present. You must handle
   *                strides in your image processing code. Strides are negative
   *                if the image is bottom-up hence why you MUST abs() it when
   *                calculating plane buffer size.
   */
  @SuppressWarnings("checkstyle:parametername")
  void receiveVideoFrame(
      int friendNumber,
      int width, int height,
      @NotNull byte[] y, @NotNull byte[] u, @NotNull byte[] v, @Nullable byte[] a,
      int yStride, int uStride, int vStride, int aStride
  );
}
