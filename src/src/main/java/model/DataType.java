package model;

import exception.SchemaException;
import exception.TypeMismatchException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public enum DataType {
    INT, DOUBLE, STRING, DATE, BOOLEAN;
    public static DataType ToDataTypeFromSql(String name) {
        return switch (name.toUpperCase()) {
            case "INT", "INTEGER" -> INT;
            case "DOUBLE", "FLOAT" -> DOUBLE;
            case "STRING", "VARCHAR", "TEXT" -> STRING;
            case "DATE" -> DATE;
            case "BOOLEAN", "BOOL" -> BOOLEAN;
            default -> throw new SchemaException("Unknown type: " + name);
        };
    }

    public Object cast(Object v) {
        if (v == null) return null;
        switch (this) {
            case INT:
                if (v instanceof Integer) return v;
                if (v instanceof Long) {
                    long l = (Long) v;
                    if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) return (int) l;
                }
                break;
            case DOUBLE:
                if (v instanceof Number) return ((Number) v).doubleValue();
                break;
            case STRING:
                if (v instanceof String) return v;
                break;
            case DATE:
                if (v instanceof LocalDate) return v;
                if (v instanceof String) {
                    try {
                        return LocalDate.parse((String) v);   // формат yyyy-MM-dd
                    } catch (DateTimeParseException e) {
                        throw new TypeMismatchException("Invalid date (expected yyyy-MM-dd): " + v);
                    }
                }
                break;
            case BOOLEAN:
                if (v instanceof Boolean) return v;
                break;
        }
        throw new TypeMismatchException("Cannot use " + v + " as " + this);
    }
}

