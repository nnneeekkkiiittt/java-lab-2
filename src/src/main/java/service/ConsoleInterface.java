package service;

import exception.DotaDBException;

import java.util.Scanner;
import java.util.function.Function;

public class ConsoleInterface {

    private final Function<String, Query> executor;
    private final Scanner scanner;

    public ConsoleInterface(Function<String, Query> executor) {
        this.executor = executor;
        this.scanner = new Scanner(System.in);
    }

    public void run() {
        System.out.println("Введите SQL-запрос или exit для выхода.");
        try {
            while (true) {
                System.out.print("> ");
                if (!scanner.hasNextLine()) {
                    break;
                }
                String line = scanner.nextLine();
                if ("exit".equalsIgnoreCase(line.trim())) {
                    break;
                }
                try {
                    Query result = executor.apply(line);
                    if (result != null && result.hasRows() && result.getRows() != null && !result.getRows().isEmpty()) {
                        TablePrinter.print(result);
                    } else {
                        System.out.println("Запрос выполнен успешно");
                    }
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
}
