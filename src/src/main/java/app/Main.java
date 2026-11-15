package app;

import parser.QueryProcessor;
import repository.FileTableRepository;
import service.ConsoleInterface;
import service.Database;
import service.DatabaseImpl;

import java.nio.file.Path;

/** Запускает консольное приложение с файловым хранилищем в каталоге {@code data}. При первом запуске создаёт демонстрационные таблицы Dota 2. */
public class Main {
    /** Создаёт точку входа приложения.
     * Конструктор не выполняет запуск; для старта используется {@link #main(String[])}.
     */
    public Main() {
    }

    /** Запускает консольную СУБД.
     * @param args аргументы командной строки (не используются).
     */
    public static void main(String[] args) {
        Database db = new DatabaseImpl(new FileTableRepository(Path.of("data")));
        if (db.getTables().isEmpty()) {
            DotaSeed.fill(db);
        }

        QueryProcessor processor = new QueryProcessor(db);
        new ConsoleInterface(processor::execute).run();
    }
}
