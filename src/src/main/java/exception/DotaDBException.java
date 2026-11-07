package exception;

public class DotaDBException extends RuntimeException {
    public DotaDBException(String message) {
        super(message);
    }
    public DotaDBException(String message, Throwable cause) { super(message, cause); }
}
