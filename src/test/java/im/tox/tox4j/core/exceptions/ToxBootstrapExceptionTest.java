package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.ToxCoreImplTestBase;
import im.tox.tox4j.core.ToxConstants;
import im.tox.tox4j.core.ToxCore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ToxBootstrapExceptionTest extends ToxCoreImplTestBase {

    @Test
    public void testBootstrapBadPort1() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.bootstrap("192.254.75.98", 0, new byte[ToxConstants.PUBLIC_KEY_SIZE]);
            fail();
        } catch (ToxBootstrapException e) {
            assertEquals(ToxBootstrapException.Code.BAD_PORT, e.getCode());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBootstrapBadPort2() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.bootstrap("192.254.75.98", -10, new byte[ToxConstants.PUBLIC_KEY_SIZE]);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBootstrapBadPort3() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.bootstrap("192.254.75.98", 65536, new byte[ToxConstants.PUBLIC_KEY_SIZE]);
        }
    }

    @Test
    public void testBootstrapBadHost() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.bootstrap(".", 33445, new byte[ToxConstants.PUBLIC_KEY_SIZE]);
            fail();
        } catch (ToxBootstrapException e) {
            assertEquals(ToxBootstrapException.Code.BAD_ADDRESS, e.getCode());
        }
    }

    @Test
    public void testBootstrapNullHost() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.bootstrap(null, 33445, new byte[ToxConstants.PUBLIC_KEY_SIZE]);
            fail();
        } catch (ToxBootstrapException e) {
            assertEquals(ToxBootstrapException.Code.NULL, e.getCode());
        }
    }

    @Test
    public void testBootstrapNullKey() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.bootstrap("localhost", 33445, null);
            fail();
        } catch (ToxBootstrapException e) {
            assertEquals(ToxBootstrapException.Code.NULL, e.getCode());
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBootstrapKeyTooShort() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.bootstrap("192.254.75.98", 33445, new byte[ToxConstants.PUBLIC_KEY_SIZE - 1]);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBootstrapKeyTooLong() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.bootstrap("192.254.75.98", 33445, new byte[ToxConstants.PUBLIC_KEY_SIZE + 1]);
        }
    }

}
