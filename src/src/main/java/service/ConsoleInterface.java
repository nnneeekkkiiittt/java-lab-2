package service;

import exception.DotaDBException;

import java.util.Scanner;
import java.util.function.Function;

/** Предоставляет интерактивный консольный интерфейс для ввода и выполнения SQL-подобных команд. */
public class ConsoleInterface {

    private final Function<String, Query> executor;
    private final Scanner scanner;

    /** Создаёт интерфейс, передавая функцию выполнения пользовательских запросов.
     * @param executor обработчик SQL-подобных запросов.
     */
    public ConsoleInterface(Function<String, Query> executor) {
        this.executor = executor;
        this.scanner = new Scanner(System.in);
    }

    /** Запускает интерактивный цикл чтения и выполнения команд до ввода {@code exit}.
     * После завершения цикла закрывает поток ввода.
     */
    public void run() {
        System.out.println("Введите SQL-запрос или exit для выхода.");
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
                if ("exit".equalsIgnoreCase(line.trim())) {
                    break;
                }
                try {
                    Query result = executor.apply(line);
                    print(result);
                } catch (DotaDBException e) {
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
