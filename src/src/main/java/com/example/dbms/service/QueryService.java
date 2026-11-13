package com.example.dbms.service;

import com.example.dbms.exception.DatabaseException;

/**
 * Сервис выполнения SQL-запросов.
 */
public interface QueryService {

    /**
     * Выполняет SQL-запрос и возвращает табличный результат (для SELECT)
     * либо {@code null}/пустой результат (для INSERT/UPDATE/DELETE).
     *
     * @param sql текст SQL-запроса
     * @return результат выборки или {@code null}
     * @throws DatabaseException если запрос не удалось выполнить
     */
    QueryResult execute(String sql) throws DatabaseException;
}
