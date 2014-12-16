package im.tox.tox4j.v2;

import im.tox.tox4j.exceptions.*;
import im.tox.tox4j.v2.exceptions.ToxBootstrapException;
import im.tox.tox4j.v2.exceptions.ToxGetPortException;
import im.tox.tox4j.v2.exceptions.ToxLoadException;

import java.io.Closeable;

public interface ToxCore extends Closeable {

    void close();

    byte[] save();

    void load(byte[] data) throws ToxLoadException;

    void bootstrap(String address, int port, byte[] public_key) throws ToxBootstrapException;

    int getPort() throws ToxGetPortException;

    int iterationTime();

    void iteration();

}
