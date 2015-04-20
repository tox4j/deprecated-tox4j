package im.tox.tox4j.core;

import im.tox.tox4j.ToxCoreImplTestBase;
import im.tox.tox4j.ToxCoreTestBase$;
import im.tox.tox4j.core.enums.ToxStatus;
import im.tox.tox4j.exceptions.ToxException;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public final class LoadSaveTest extends ToxCoreImplTestBase {

    private interface Check {
        boolean change(ToxCore tox) throws ToxException;
        void check(ToxCore tox);
    }

    private void testLoadSave(Check check) throws Exception {
        boolean moreTests = true;
        while (moreTests) {
            byte[] data;
            try (ToxCore tox = newTox()) {
                moreTests = check.change(tox);
                data = tox.save();
            }
            try (ToxCore tox = newTox(data)) {
                check.check(tox);
            }
        }
    }

    @Test
    public void testName() throws Exception {
        testLoadSave(new Check() {
            private byte[] expected = {};

            @Override
            public boolean change(ToxCore tox) throws ToxException {
                if (expected == null) {
                    expected = new byte[0];
                } else {
                    expected = ToxCoreTestBase$.MODULE$.randomBytes(expected.length + 1);
                }
                tox.setName(expected);
                return expected.length < ToxConstants.MAX_NAME_LENGTH;
            }

            @Override
            public void check(ToxCore tox) {
                assertArrayEquals(expected, tox.getName());
            }
        });
    }

    @Test
    public void testStatusMessage() throws Exception {
        testLoadSave(new Check() {
            private byte[] expected = null;

            @Override
            public boolean change(ToxCore tox) throws ToxException {
                if (expected == null) {
                    expected = new byte[0];
                } else {
                    expected = ToxCoreTestBase$.MODULE$.randomBytes(expected.length + 1);
                }
                tox.setStatusMessage(expected);
                return expected.length < ToxConstants.MAX_NAME_LENGTH;
            }

            @Override
            public void check(ToxCore tox) {
                assertArrayEquals(expected, tox.getStatusMessage());
            }
        });
    }

    @Test
    public void testStatus() throws Exception {
        testLoadSave(new Check() {
            private final List<ToxStatus> expected = new ArrayList<>(Arrays.asList(ToxStatus.values()));

            @Override
            public boolean change(ToxCore tox) throws ToxException {
                tox.setStatus(expected.get(expected.size() - 1));
                return expected.size() > 1;
            }

            @Override
            public void check(ToxCore tox) {
                assertEquals(expected.remove(expected.size() - 1), tox.getStatus());
            }
        });
    }

    @Test
    public void testNoSpam() throws Exception {
        testLoadSave(new Check() {
            private int expected = -1;

            @Override
            public boolean change(ToxCore tox) throws ToxException {
                tox.setNospam(++expected);
                return expected < 100;
            }

            @Override
            public void check(ToxCore tox) {
                assertEquals(expected, tox.getNospam());
            }
        });
    }

    @Test
    public void testFriend() throws Exception {
        testLoadSave(new Check() {
            private int expected;

            @Override
            public boolean change(ToxCore tox) throws ToxException {
                try (ToxCore toxFriend = newTox()) {
                    expected = tox.addFriend(toxFriend.getAddress(), "hello".getBytes());
                }
                return false;
            }

            @Override
            public void check(ToxCore tox) {
                assertEquals(1, tox.getFriendList().length);
                assertEquals(expected, tox.getFriendList()[0]);
            }
        });
    }

    @Test
    public void testSaveNotEmpty() throws Exception {
        try (ToxCore tox = newTox()) {
            byte[] data = tox.save();
            assertNotNull(data);
            assertNotEquals(0, data.length);
        }
    }

    @Test
    public void testSaveRepeatable() throws Exception {
        try (ToxCore tox = newTox()) {
            assertArrayEquals(tox.save(), tox.save());
        }
    }

    @Test
    public void testLoadSave1() throws Exception {
        byte[] data = newTox().save();
        assertArrayEquals(newTox(data).save(), newTox(data).save());
    }

    @Test
    public void testLoadSave2() throws Exception {
        byte[] data = newTox().save();
        assertEquals(data.length, newTox(data).save().length);
    }

    @Test
    public void testLoadSave3() throws Exception {
        byte[] data = newTox().save();
        assertArrayEquals(data, newTox(data).save());
    }

}
