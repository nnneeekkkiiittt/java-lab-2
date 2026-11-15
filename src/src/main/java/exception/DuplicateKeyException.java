package exception;

/** Сообщает о нарушении уникальности первичного ключа. */
public class DuplicateKeyException extends DatabaseException {
    /** Создаёт исключение: Нарушение уникальности первичного ключа.
     * @param message подробное описание причины ошибки.
     */
    public DuplicateKeyException(String message) {
        super(message);
    }
}
