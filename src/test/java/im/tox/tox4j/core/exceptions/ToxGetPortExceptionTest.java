package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.ToxCoreImplTestBase;
import im.tox.tox4j.core.ToxCore;
import im.tox.tox4j.core.enums.ToxProxyType;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ToxGetPortExceptionTest extends ToxCoreImplTestBase {

    @Test
    public void testGetTcpPort_NotBound() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.getTcpPort();
            fail();
        } catch (ToxGetPortException e) {
            assertEquals(ToxGetPortException.Code.NOT_BOUND, e.getCode());
        }
    }

}
