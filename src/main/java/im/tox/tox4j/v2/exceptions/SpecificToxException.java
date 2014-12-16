package im.tox.tox4j.v2.exceptions;

public abstract class SpecificToxException extends Exception {

    public abstract Enum<?> getCode();

    @Override
    public String getMessage() {
        return "Error code: " + getCode().name();
    }
}
