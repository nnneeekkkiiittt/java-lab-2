package exception;

/** Сообщает о нарушении ограничения внешнего ключа. */
public class ForeignKeyException extends DotaDBException {
    /** Создаёт исключение: Нарушение ограничения внешнего ключа.
     * @param message подробное описание причины ошибки.
     */
    public ForeignKeyException(String message) {
        super(message);
    }
}
