package exception;

/** Сообщает об ошибке определения или использования схемы таблицы. */
public class SchemaException extends DatabaseException {
    /** Создаёт исключение: Ошибка определения или использования схемы таблицы.
     * @param message подробное описание причины ошибки.
     */
    public SchemaException(String message) {
        super(message);
    }
}
