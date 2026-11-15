package parser.statement;

import model.Condition;
import service.Database;
import service.Query;

import java.util.Map;

/** Представляет разобранную команду UPDATE ... SET. */
public final class UpdateStatement implements Statement {
    private final String table;
    private final Map<String, Object> assignments;   // колонка -> новое значение
    private final Condition where;                    // null означает все строки

    /** Создаёт команду обновления данных.
     * @param table имя таблицы.
     * @param assignments отображение имён колонок на новые значения.
     * @param where условие обновления или {@code null} для всех строк.
     */
    public UpdateStatement(String table, Map<String, Object> assignments, Condition where) {
        this.table = table;
        this.assignments = assignments;
        this.where = where;
    }

    @Override
    public Query execute(Database db) {
        return Query.ofAffected(db.update(table, assignments, where));
    }
}
