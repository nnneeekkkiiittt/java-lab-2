package exception;

/** Базовое непроверяемое исключение для ошибок, возникающих при работе СУБД. */
public class DatabaseException extends RuntimeException {
    /** Создаёт исключение с описанием ошибки СУБД.
     * @param message сообщение об ошибке.
     */
    public DatabaseException(String message) {
        super(message);
    }
    /** Создаёт исключение с описанием и исходной причиной ошибки.
     * @param message сообщение об ошибке.
     * @param cause исключение, вызвавшее текущую ошибку.
     */
    public DatabaseException(String message, Throwable cause) { super(message, cause); }
}
