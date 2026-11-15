package exception;

/** Сообщает о невозможности привести значение к типу колонки. */
public class TypeMismatchException extends DatabaseException {
    /** Создаёт исключение: Несовместимость значения с объявленным типом колонки.
     * @param message подробное описание причины ошибки.
     */
    public TypeMismatchException(String message) {
        super(message);
    }
}
