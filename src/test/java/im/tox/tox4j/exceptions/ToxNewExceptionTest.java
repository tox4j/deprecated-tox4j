package im.tox.tox4j.exceptions;

import im.tox.tox4j.ToxCore;
import im.tox.tox4j.ToxCoreImplTestBase;
import im.tox.tox4j.enums.ToxProxyType;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class ToxNewExceptionTest extends ToxCoreImplTestBase {

    @Test
    public void testToxNewProxyNull() throws Exception {
        try {
            newTox(true, true, ToxProxyType.SOCKS5, null, 0).close();
            fail();
        } catch (ToxNewException e) {
            assertEquals(ToxNewException.Code.NULL, e.getCode());
        }
    }

    @Test
    public void testToxNewProxyEmpty() throws Exception {
        try {
            newTox(true, true, ToxProxyType.SOCKS5, "", 1).close();
            fail();
        } catch (ToxNewException e) {
            assertEquals(ToxNewException.Code.PROXY_BAD_HOST, e.getCode());
        }
    }

    @Test
    public void testToxNewProxyBadPort0() throws Exception {
        try {
            newTox(true, true, ToxProxyType.SOCKS5, "localhost", 0).close();
            fail();
        } catch (ToxNewException e) {
            assertEquals(ToxNewException.Code.PROXY_BAD_PORT, e.getCode());
        }
    }

    @Test
    public void testToxNewProxyBadPortNegative() {
        try {
            newTox(true, true, ToxProxyType.SOCKS5, "localhost", -10).close();
            fail();
        } catch (ToxNewException e) {
            assertEquals(ToxNewException.Code.PROXY_BAD_PORT, e.getCode());
        }
    }

    @Test
    public void testToxNewProxyBadPortTooLarge() throws Exception {
        try {
            newTox(true, true, ToxProxyType.SOCKS5, "localhost", 0x10000).close();
            fail();
        } catch (ToxNewException e) {
            assertEquals(ToxNewException.Code.PROXY_BAD_PORT, e.getCode());
        }
    }

    @Test
    public void testToxNewProxyBadAddress1() throws Exception {
        try {
            newTox(true, true, ToxProxyType.SOCKS5, "\u2639", 1).close();
            fail();
        } catch (ToxNewException e) {
            assertEquals(ToxNewException.Code.PROXY_BAD_HOST, e.getCode());
        }
    }

    @Test
    public void testToxNewProxyBadAddress2() throws Exception {
        try {
            newTox(true, true, ToxProxyType.SOCKS5, ".", 1).close();
            fail();
        } catch (ToxNewException e) {
            assertEquals(ToxNewException.Code.PROXY_BAD_HOST, e.getCode());
        }
    }

    @Test
    public void testTooManyToxCreations() throws Exception {
        try {
            ArrayList<ToxCore> toxes = new ArrayList<>();
            for (int i = 0; i < 102; i++) {
                // One of these will fail.
                toxes.add(newTox());
            }
            // If nothing fails, clean up and fail.
            for (ToxCore tox : toxes) {
                tox.close();
            }
            fail();
        } catch (ToxNewException e) {
            assertEquals(ToxNewException.Code.PORT_ALLOC, e.getCode());
        }
    }

}