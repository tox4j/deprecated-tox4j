package im.tox.tox4j;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.annotations.Nullable;
import im.tox.tox4j.callbacks.*;
import im.tox.tox4j.enums.ToxFileControl;
import im.tox.tox4j.enums.ToxFileKind;
import im.tox.tox4j.enums.ToxStatus;
import im.tox.tox4j.exceptions.*;

import java.io.Closeable;

public interface ToxAV extends Closeable {

    @Override
    void close();

    int iterationInterval();

    void iteration();

}
