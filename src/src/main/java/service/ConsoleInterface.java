package service;

import exception.DatabaseException;

import java.util.Scanner;
import java.util.function.Function;

/** Предоставляет интерактивный консольный интерфейс для ввода и выполнения SQL-подобных команд. */
public class ConsoleInterface {

    private static final int PREVIEW_ROWS = 5;

    private final Function<String, Query> executor;
    private final Database database;
    private final Scanner scanner;

    /** Создаёт интерфейс, передавая функцию выполнения пользовательских запросов.
     * @param executor обработчик SQL-подобных запросов.
     */
    public ConsoleInterface(Function<String, Query> executor) {
        this(executor, null);
    }

    /** Создаёт интерфейс, который при запуске и по команде {@code tables} показывает таблицы базы.
     * @param executor обработчик SQL-подобных запросов.
     * @param database база, таблицы которой показываются пользователю; {@code null}, чтобы отключить показ.
     */
    public ConsoleInterface(Function<String, Query> executor, Database database) {
        this.executor = executor;
        this.database = database;
        this.scanner = new Scanner(System.in);
    }

    /** Показывает существующие таблицы и справку по синтаксису, затем запускает интерактивный цикл
     * чтения и выполнения команд до ввода {@code exit}. Кроме SQL-команд понимает {@code help}
     * (справка) и {@code tables} (показать таблицы). После завершения цикла закрывает поток ввода.
     */
    public void run() {
        if (database != null) {
            TablePreview.print(database, PREVIEW_ROWS);
            System.out.println();
        }
        SyntaxHelp.print();
        System.out.println();
        System.out.println("Введите SQL-запрос, help, tables или exit.");
        try {
            while (true) {
                System.out.print("> ");
                if (!scanner.hasNextLine()) {
                    break;
                }
                String line = scanner.nextLine();
                if (line.trim().isEmpty()) {
                    continue;
                }
                String command = line.trim();
                if (command.endsWith(";")) {
                    command = command.substring(0, command.length() - 1).trim();
                }
                if ("exit".equalsIgnoreCase(command)) {
                    break;
                }
                if ("help".equalsIgnoreCase(command)) {
                    SyntaxHelp.print();
                    continue;
                }
                if ("tables".equalsIgnoreCase(command) && database != null) {
                    TablePreview.print(database, PREVIEW_ROWS);
                    continue;
                }
                try {
                    Query result = executor.apply(line);
                    print(result);
                } catch (DatabaseException e) {
                    System.out.println("Ошибка: " + e.getMessage());
                } catch (RuntimeException e) {
                    System.out.println("Непредвиденная ошибка: " + e.getMessage());
                }
            }
        } finally {
            scanner.close();
        }
    }

    private static void print(Query result) {
        if (result == null) {
            System.out.println("Запрос выполнен");
        } else if (result.hasRows()) {
            TablePrinter.print(result);
            System.out.println("Строк: " + result.getRows().size());
        } else if (result.getMessage() != null) {
            System.out.println(result.getMessage());
        } else {
            System.out.println("Затронуто строк: " + result.getAffected());
        }
    }
}
