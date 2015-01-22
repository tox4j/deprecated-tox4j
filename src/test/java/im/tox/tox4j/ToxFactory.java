package im.tox.tox4j;

import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.ToxCore;
import im.tox.tox4j.core.exceptions.ToxNewException;

public interface ToxFactory {

    @NotNull ToxCore newTox(boolean ipv6Enabled, boolean udpEnabled) throws ToxNewException;

}
