package terminalBuffer;

public class CursorOutOfBoundaryException extends RuntimeException {
    public CursorOutOfBoundaryException(String message) {
        super(message);
    }
}
