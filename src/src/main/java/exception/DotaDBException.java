package exception;

/** Базовое непроверяемое исключение для ошибок, возникающих при работе СУБД. */
public class DotaDBException extends RuntimeException {
    /** Создаёт исключение с описанием ошибки СУБД.
     * @param message сообщение об ошибке.
     */
    public DotaDBException(String message) {
        super(message);
    }
    /** Создаёт исключение с описанием и исходной причиной ошибки.
     * @param message сообщение об ошибке.
     * @param cause исключение, вызвавшее текущую ошибку.
     */
    public DotaDBException(String message, Throwable cause) { super(message, cause); }
}
