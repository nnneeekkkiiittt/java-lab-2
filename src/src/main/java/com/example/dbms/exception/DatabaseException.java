package com.example.dbms.exception;

/**
 * Исключение, сигнализирующее об ошибке выполнения операции СУБД.
 */
public class DatabaseException extends Exception {

    /**
     * Создаёт исключение с описанием ошибки.
     *
     * @param message текст ошибки
     */
    public DatabaseException(String message) {
        super(message);
    }
}
