package im.tox.tox4j.av;

import im.tox.tox4j.ToxAvImplTestBase;
import org.junit.Test;

import static org.junit.Assert.*;

public class ToxAvTest extends ToxAvImplTestBase {

    @Test
    public void testClose() throws Exception {
        newToxAv().close();
    }

    @Test
    public void testIterationInterval() throws Exception {
        try (ToxAv av = newToxAv()) {
            assertNotEquals(0, av.iterationInterval());
            assertTrue(av.iterationInterval() > 0);
            assertTrue(av.iterationInterval() < 1000);
        }
    }

    @Test
    public void testIteration() throws Exception {
        try (ToxAv av = newToxAv()) {
            av.iteration();
        }
    }

}