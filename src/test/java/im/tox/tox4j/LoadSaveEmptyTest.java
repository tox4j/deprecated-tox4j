package im.tox.tox4j;

import im.tox.tox4j.enums.ToxStatus;
import im.tox.tox4j.exceptions.ToxNewException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LoadSaveEmptyTest extends ToxCoreTestBase {

    private final byte[] emptySave;

    public LoadSaveEmptyTest () throws ToxNewException {
        byte[] emptySave;
        try (ToxCore tox = newTox()) {
            this.emptySave = tox.save();
        }
    }

    @Override
    public ToxCore newTox(ToxOptions options) throws ToxNewException {
        return new ToxCoreImpl(options);
    }

    @Test
    public void testName() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.setName("hello".getBytes());
            tox.load(emptySave);
            assertEquals(null, tox.getName());
        }
    }

    @Test
    public void testStatus() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.setStatus(ToxStatus.AWAY);
            tox.load(emptySave);
            assertEquals(ToxStatus.NONE, tox.getStatus());
        }
    }

    @Test
    public void testStatusMessage() throws Exception {
        try (ToxCore tox = newTox()) {
            tox.setStatusMessage("hello".getBytes());
            tox.load(emptySave);
            assertEquals(null, tox.getStatusMessage());
        }
    }

}
