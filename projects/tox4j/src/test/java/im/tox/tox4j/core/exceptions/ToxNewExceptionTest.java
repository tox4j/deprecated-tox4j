package im.tox.tox4j.core.exceptions;

import im.tox.tox4j.ToxCoreTestBase;
import im.tox.tox4j.core.ToxCore;
import im.tox.tox4j.core.options.ProxyOptions;
import org.junit.Test;
import scala.runtime.BoxedUnit;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

public class ToxNewExceptionTest extends ToxCoreTestBase {

  @Test
  public void testToxNewProxyNull() throws Exception {
    try {
      newTox(true, true, new ProxyOptions.Socks5(null, 1)).close();
      fail();
    } catch (ToxNewException e) {
      assertEquals(ToxNewException.Code.PROXY_BAD_HOST, e.code());
    }
  }

  @Test
  public void testToxNewProxyEmpty() throws Exception {
    try {
      newTox(true, true, new ProxyOptions.Socks5("", 1)).close();
      fail();
    } catch (ToxNewException e) {
      assertEquals(ToxNewException.Code.PROXY_BAD_HOST, e.code());
    }
  }

  @Test
  public void testToxNewProxyBadPort0() throws Exception {
    try {
      newTox(true, true, new ProxyOptions.Socks5("localhost", 0)).close();
      fail();
    } catch (ToxNewException e) {
      assertEquals(ToxNewException.Code.PROXY_BAD_PORT, e.code());
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testToxNewProxyBadPortNegative() throws Exception {
    newTox(true, true, new ProxyOptions.Socks5("localhost", -10)).close();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testToxNewProxyBadPortTooLarge() throws Exception {
    newTox(true, true, new ProxyOptions.Socks5("localhost", 0x10000)).close();
  }

  @SuppressWarnings("checkstyle:avoidescapedunicodecharacters")
  @Test
  public void testToxNewProxyBadAddress1() throws Exception {
    try {
      newTox(true, true, new ProxyOptions.Socks5("\u2639", 1)).close();
      fail();
    } catch (ToxNewException e) {
      assertEquals(ToxNewException.Code.PROXY_BAD_HOST, e.code());
    }
  }

  @Test
  public void testToxNewProxyBadAddress2() throws Exception {
    try {
      newTox(true, true, new ProxyOptions.Socks5(".", 1)).close();
      fail();
    } catch (ToxNewException e) {
      assertEquals(ToxNewException.Code.PROXY_BAD_HOST, e.code());
    }
  }

  @Test
  public void testTooManyToxCreations() throws Exception {
    try {
      Collection<ToxCore<BoxedUnit>> toxes = new ArrayList<>();
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
      assertEquals(ToxNewException.Code.PORT_ALLOC, e.code());
    }
  }

  @Test
  public void testLoadEncrypted() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox("toxEsave blah blah blah".getBytes())) {
      fail();
    } catch (ToxNewException e) {
      assertEquals(ToxNewException.Code.LOAD_ENCRYPTED, e.code());
    }
  }

  @Test
  public void testLoadBadFormat() throws Exception {
    try (ToxCore<BoxedUnit> tox = newTox("blah blah blah".getBytes())) {
      fail();
    } catch (ToxNewException e) {
      assertEquals(ToxNewException.Code.LOAD_BAD_FORMAT, e.code());
    }
  }

}
