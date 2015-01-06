package im.tox.tox4j.exceptions;

import im.tox.tox4j.ToxCoreImplTestBase;
import im.tox.tox4j.core.ToxCore;
import org.junit.Test;

import static org.junit.Assert.fail;

public class ToxKilledExceptionTest extends ToxCoreImplTestBase {

    @Test(expected=ToxKilledException.class)
    public void testClose_DoubleCloseThrows() throws Exception {
        ToxCore tox = newTox();
        try {
            tox.close();
        } catch (ToxKilledException e) {
            fail("The first close should not have thrown");
        }
        tox.close();
    }

    @Test(expected=ToxKilledException.class)
    public void testDoubleCloseError() throws Exception {
        ToxCore tox1 = newTox();
        tox1.close();
        newTox();
        tox1.close(); // Should throw.
    }

    @Test(expected=ToxKilledException.class)
    public void testDoubleCloseInOrder() throws Exception {
        ToxCore tox1 = newTox();
        ToxCore tox2 = newTox();
        tox1.close();
        tox1.close();
    }

    @Test(expected=ToxKilledException.class)
    public void testDoubleCloseReverseOrder() throws Exception {
        ToxCore tox1 = newTox();
        ToxCore tox2 = newTox();
        tox2.close();
        tox2.close();
    }

    @Test(expected=ToxKilledException.class)
    public void testUseAfterCloseInOrder() throws Exception {
        ToxCore tox1 = newTox();
        ToxCore tox2 = newTox();
        tox1.close();
        tox1.iterationInterval();
    }

    @Test(expected=ToxKilledException.class)
    public void testUseAfterCloseReverseOrder() throws Exception {
        ToxCore tox1 = newTox();
        ToxCore tox2 = newTox();
        tox2.close();
        tox2.iterationInterval();
    }

}
