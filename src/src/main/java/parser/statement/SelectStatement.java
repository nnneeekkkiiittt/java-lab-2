package parser.statement;

import model.Condition;
import service.Database;
import service.Query;

import java.util.List;

/** Представляет разобранную команду SELECT с необязательным условием WHERE. */
public final class SelectStatement implements Statement {
    private final String table;
    private final List<String> columns;   // null означает все колонки (SELECT *)
    private final Condition where;        // null означает без WHERE

    /** Создаёт команду выборки данных.
     * @param table имя таблицы.
     * @param columns список колонок или {@code null} для выбора всех колонок.
     * @param where условие фильтрации или {@code null} для выборки без условия.
     */
    public SelectStatement(String table, List<String> columns, Condition where) {
        this.table = table;
        this.columns = columns;
        this.where = where;
    }

    @Override
    public Query execute(Database db) {
        return db.select(table, columns, where);
    }
}
