package server.model.exception;

public class StateAlreadySetException extends IllegalArgumentException {
    public StateAlreadySetException(String s) {
        super(s);
    }
}
