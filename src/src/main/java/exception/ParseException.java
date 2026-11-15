package exception;

/** Ошибка разбора SQL-запроса: неизвестная команда, пропущенный токен, незакрытая строка и т.п. */
public class ParseException extends DotaDBException {
    /** Создаёт исключение: Ошибка разбора SQL-подобного запроса.
     * @param message подробное описание причины ошибки.
     */
    public ParseException(String message) {
        super(message);
    }
}
