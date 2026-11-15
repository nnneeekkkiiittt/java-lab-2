package service;

import java.util.List;

/** Представляет результат SELECT, число затронутых строк либо сообщение об исполнении DDL-команды. */
public class Query{
    private final List<String> columns;   // null, если это не SELECT
    private final List<Object[]> rows;
    private final int affected;
    private final String message;         // текст для CREATE/DROP, иначе null

    private Query(List<String> columns, List<Object[]> rows, int affected, String message) {
        this.columns = columns;
        this.rows = rows;
        this.affected = affected;
        this.message = message;
    }

    /** Создаёт результат выборки строк.
     * @param columns названия выбранных колонок.
     * @param rows значения строк в порядке колонок.
     * @return объект результата SELECT.
     */
    public static Query ofRows(List<String> columns, List<Object[]> rows) {
        return new Query(columns, rows, rows.size(), null);
    }

    /** Создаёт результат команды, изменившей заданное количество строк.
     * @param affected число затронутых строк.
     * @return объект результата команды.
     */
    public static Query ofAffected(int affected) {
        return new Query(null, null, affected, null);
    }

    /** Создаёт результат DDL-команды или другого действия с текстовым сообщением.
     * @param message сообщение о результате выполнения.
     * @return объект результата с сообщением.
     */
    public static Query ofMessage(String message) {
        return new Query(null, null, 0, message);
    }

    /** Проверяет, содержит ли результат табличные строки.
     * @return {@code true}, если результат является выборкой.
     */
    public boolean hasRows()          { return columns != null; }

    /** Возвращает названия колонок результата.
     * @return список названий колонок или {@code null}, если результат не является выборкой.
     */
    public List<String> getColumns()  { return columns; }

    /** Возвращает строки результата выборки.
     * @return список строк или {@code null}, если результат не является выборкой.
     */
    public List<Object[]> getRows()   { return rows; }

    /** Возвращает число затронутых записей.
     * @return число добавленных, обновлённых или удалённых строк.
     */
    public int getAffected()          { return affected; }

    /** Возвращает текстовое сообщение результата.
     * @return сообщение или {@code null}, если оно не предусмотрено.
     */
    public String getMessage()        { return message; }
}
