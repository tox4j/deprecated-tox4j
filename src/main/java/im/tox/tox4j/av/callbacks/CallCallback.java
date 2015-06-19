package im.tox.tox4j.av.callbacks;

public interface CallCallback {

  CallCallback EMPTY = new CallCallback() {
    @Override
    public void call(int friendNumber, boolean audioEnabled, boolean videoEnabled) {
    }
  };

  void call(int friendNumber, boolean audioEnabled, boolean videoEnabled);

}
