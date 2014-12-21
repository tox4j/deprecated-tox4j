package im.tox.tox4j.exceptions;

import im.tox.tox4j.ToxCore;
import im.tox.tox4j.ToxCoreImplTestBase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ToxLoadExceptionTest extends ToxCoreImplTestBase {

    @Test
    public void testNULL() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.load(null);
            fail();
        } catch (ToxLoadException e) {
            assertEquals(ToxLoadException.Code.NULL, e.getCode());
        }
    }

    @Test
    public void testENCRYPTED() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.load("toxEsave blah blah blah".getBytes());
            fail();
        } catch (ToxLoadException e) {
            assertEquals(ToxLoadException.Code.ENCRYPTED, e.getCode());
        }
    }

    @Test
    public void testBAD_FORMAT() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.load("blah blah blah".getBytes());
            fail();
        } catch (ToxLoadException e) {
            assertEquals(ToxLoadException.Code.BAD_FORMAT, e.getCode());
        }
    }

}