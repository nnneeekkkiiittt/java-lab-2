package parser.statement;

import service.Database;
import service.Query;

/** Представляет разобранную команду DROP TABLE. */
public final class DropTableStatement implements Statement {
    private final String table;

    /** Создаёт команду удаления таблицы.
     * @param table имя удаляемой таблицы.
     */
    public DropTableStatement(String table) {
        this.table = table;
    }

    @Override
    public Query execute(Database db) {
        db.dropTable(table);
        return Query.ofMessage("Таблица " + table + " удалена");
    }
}
