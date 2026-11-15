package parser.statement;

import model.Schema;
import service.Database;
import service.Query;

public final class CreateTableStatement implements Statement {
    private final Schema schema;

    public CreateTableStatement(Schema schema) {
        this.schema = schema;
    }

    @Override
    public Query execute(Database db) {
        db.createTable(schema);
        return Query.ofMessage("Таблица " + schema.getTableName() + " создана");
    }
}
