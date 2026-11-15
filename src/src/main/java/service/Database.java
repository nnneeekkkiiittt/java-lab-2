package service;

import model.Condition;
import model.Schema;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/** Определяет публичный контракт операций упрощённой реляционной СУБД. */
public interface Database {
    /** Создаёт таблицу по заданной схеме.
     * @param schema схема новой таблицы.
     */
    void createTable(Schema schema);
    /** Удаляет таблицу, если на неё не ссылаются внешние ключи.
     * @param table имя таблицы.
     */
    void dropTable(String table);
    /** Вставляет одну или несколько записей.
     * @param table имя таблицы.
     * @param columns список передаваемых колонок или {@code null} для всех колонок.
     * @param rows значения вставляемых записей.
     * @return число добавленных строк.
     */
    int insert(String table, List<String> columns, List<List<Object>> rows);
    /** Выполняет выборку с необязательным условием фильтрации.
     * @param table имя таблицы.
     * @param columns список выбираемых колонок или {@code null} для всех колонок.
     * @param where условие WHERE или {@code null}.
     * @return результат SELECT.
     */
    Query select(String table, List<String> columns, Condition where);
    /** Обновляет значения указанных колонок в подходящих строках.
     * @param table имя таблицы.
     * @param assignments карта «имя колонки — новое значение».
     * @param where условие WHERE или {@code null} для всех строк.
     * @return число обновлённых строк.
     */
    int update(String table, Map<String, Object> assignments, Condition where);
    /** Удаляет записи, подходящие под условие.
     * @param table имя таблицы.
     * @param where условие WHERE или {@code null} для всех строк.
     * @return число удалённых строк.
     */
    int delete(String table, Condition where);
    /** Создаёт вторичный индекс по указанной колонке.
     * @param indexName имя индекса.
     * @param table имя таблицы.
     * @param column индексируемая колонка.
     */
    void createIndex(String indexName, String table, String column);
    /** Возвращает все таблицы базы данных.
     * @return коллекция таблиц только для чтения.
     */
    Collection<Table> getTables();
}
