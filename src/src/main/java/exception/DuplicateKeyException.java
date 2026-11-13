package exception;

public class DuplicateKeyException extends DotaDBException {
    public DuplicateKeyException(String message) {
        super(message);
    }
}
