package im.tox.tox4j.av.callbacks;

public interface CallCallback {

  void call(int friendNumber, boolean audioEnabled, boolean videoEnabled);

}
