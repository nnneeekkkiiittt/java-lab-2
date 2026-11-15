package parser.statement;

import service.Database;
import service.Query;

/** Разобранный запрос. Паттерн Command: знает, какой метод {@link Database} вызвать. */
public interface Statement {
    Query execute(Database db);
}
