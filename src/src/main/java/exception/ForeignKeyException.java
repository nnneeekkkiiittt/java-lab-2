package exception;

/** Сообщает о нарушении ограничения внешнего ключа. */
public class ForeignKeyException extends DatabaseException {
    /** Создаёт исключение: Нарушение ограничения внешнего ключа.
     * @param message подробное описание причины ошибки.
     */
    public ForeignKeyException(String message) {
        super(message);
    }
}
