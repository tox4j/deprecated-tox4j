package im.tox.tox4j.av;

import im.tox.tox4j.*;
import im.tox.tox4j.annotations.NotNull;
import im.tox.tox4j.core.ToxCore;
import im.tox.tox4j.core.ToxOptions;
import im.tox.tox4j.core.exceptions.ToxNewException;
import org.junit.Test;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

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
