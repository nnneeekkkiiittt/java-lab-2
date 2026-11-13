package exception;

public class ForeignKeyException extends DotaDBException {
    public ForeignKeyException(String message) {
        super(message);
    }
}
