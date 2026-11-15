package model;

import java.util.function.Predicate;

/** Хранит одно условие сравнения для фильтрации строк таблицы. */
public class Condition {
    private final String column;
    private final Operator operator;
    private final Object value;

    /** Создаёт условие сравнения.
     * @param column имя колонки.
     * @param operator оператор сравнения.
     * @param value сравниваемый литерал.
     */
    public Condition(String column, Operator operator, Object value) {
        this.column = column.toLowerCase();
        this.operator = operator;
        this.value = value;
    }

    /** Возвращает имя колонки, к которой применяется условие.
     * @return имя колонки.
     */
    public String getColumn()     { return column; }
    /** Возвращает оператор сравнения условия.
     * @return оператор сравнения.
     */
    public Operator getOperator() { return operator; }
    /** Возвращает литерал, с которым сравнивается значение колонки.
     * @return значение литерала.
     */
    public Object getValue()      { return value; }

    /** Создаёт предикат для строк указанной схемы.
     * @param schema схема таблицы.
     * @return предикат проверки строки.
     */
    public Predicate<Row> bind(Schema schema) {
        int pos = schema.indexOf(column);
        DataType type = schema.getColumns().get(pos).getType();
        Object literal = prepare(type, value);
        Operator op = operator;

        return row -> {
            Object actual = row.get(pos);
            if (actual == null || literal == null) return false;  // как в SQL: сравнение с NULL ложно
            return op.compare(actual, literal);
        };
    }

    private static Object prepare(DataType type, Object literal) {
        if (literal == null) return null;
        if ((type == DataType.INT || type == DataType.DOUBLE) && literal instanceof Number) {
            return literal;
        }
        return type.cast(literal);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static int compare(Object a, Object b) {
        if (a instanceof Number && b instanceof Number) {
            return Double.compare(((Number) a).doubleValue(), ((Number) b).doubleValue());
        }
        return ((Comparable) a).compareTo(b);
    }

    @Override public String toString() {
        return column + " " + operator.getSymbol() + " " + value;
    }
}
