package app;

import exception.DotaDBException;
import repository.FileTableRepository;
import service.ConsoleInterface;
import service.Database;
import service.DatabaseImpl;

import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        Database db = new DatabaseImpl(new FileTableRepository(Path.of("data")));
        if (db.getTables().isEmpty()) {
            DotaSeed.fill(db);
        }

        // TODO: заменить на парсер: sql -> QueryProcessor.execute(sql)
        new ConsoleInterface(sql -> {
            throw new DotaDBException("SQL parser is not connected yet");
        }).run();
    }
}
