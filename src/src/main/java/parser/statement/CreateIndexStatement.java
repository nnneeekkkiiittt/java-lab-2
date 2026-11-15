package parser.statement;

import service.Database;
import service.Query;

/** Представляет разобранную команду CREATE INDEX. */
public final class CreateIndexStatement implements Statement {
    private final String indexName;
    private final String table;
    private final String column;

    /** Создаёт команду создания индекса.
     * @param indexName имя нового индекса.
     * @param table имя таблицы.
     * @param column имя индексируемой колонки.
     */
    public CreateIndexStatement(String indexName, String table, String column) {
        this.indexName = indexName;
        this.table = table;
        this.column = column;
    }

    @Override
    public Query execute(Database db) {
        db.createIndex(indexName, table, column);
        return Query.ofMessage("Индекс " + indexName + " создан");
    }
}
