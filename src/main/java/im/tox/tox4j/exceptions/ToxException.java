package im.tox.tox4j.exceptions;

public abstract class ToxException extends Exception {

    public abstract Enum<?> getCode();

    @Override
    public String getMessage() {
        return "Error code: " + getCode();
    }

}
