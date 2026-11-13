package com.example.dbms.cli;

import com.example.dbms.exception.DatabaseException;
import com.example.dbms.service.QueryResult;
import com.example.dbms.service.QueryService;
import com.example.dbms.util.TablePrinter;

import java.util.List;
import java.util.Scanner;

/**
 * Консольный интерфейс упрощённой СУБД: читает SQL из stdin и выводит результат.
 */
public class ConsoleInterface {

    private final QueryService queryService;
    private final Scanner scanner;

    /**
     * Создаёт CLI, связанный с сервисом выполнения запросов.
     *
     * @param queryService сервис, которому делегируется SQL
     */
    public ConsoleInterface(QueryService queryService) {
        this.queryService = queryService;
        this.scanner = new Scanner(System.in);
    }

    /**
     * Запускает главный цикл: приглашение, чтение строк, выполнение запросов до команды {@code exit}.
     */
    public void run() {
        System.out.println("Добро пожаловать в упрощённую СУБД (Dota 2).");
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
                    QueryResult result = queryService.execute(line);
                    if (hasRows(result)) {
                        TablePrinter.print(result);
                    } else {
                        System.out.println("Запрос выполнен успешно");
                    }
                } catch (DatabaseException e) {
                    System.out.println("Ошибка: " + e.getMessage());
                } catch (Exception e) {
                    System.out.println("Непредвиденная ошибка: " + e.getMessage());
                }
            }
        } finally {
            scanner.close();
        }
    }

    private static boolean hasRows(QueryResult result) {
        if (result == null) {
            return false;
        }
        List<List<Object>> rows = result.getRows();
        return rows != null && !rows.isEmpty();
    }
}
