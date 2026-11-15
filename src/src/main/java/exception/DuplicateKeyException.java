package exception;

/** Сообщает о нарушении уникальности первичного ключа. */
public class DuplicateKeyException extends DotaDBException {
    /** Создаёт исключение: Нарушение уникальности первичного ключа.
     * @param message подробное описание причины ошибки.
     */
    public DuplicateKeyException(String message) {
        super(message);
    }
}
