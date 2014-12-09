package im.tox.tox4j;

import im.tox.tox4j.exceptions.ToxException;

import static org.junit.Assert.*;

public class Tox4jTest extends ToxSimpleChatTest {

    @Override
    protected ToxSimpleChat newTox() throws ToxException {
        return new Tox4j();
    }

}