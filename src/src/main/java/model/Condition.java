package model;

import java.util.function.Predicate;

public class Condition {
    private final String column;
    private final Operator operator;
    private final Object value;

    public Condition(String column, Operator operator, Object value) {
        this.column = column.toLowerCase();
        this.operator = operator;
        this.value = value;
    }

    public String getColumn()     { return column; }
    public Operator getOperator() { return operator; }
    public Object getValue()      { return value; }

    public Predicate<Row> bind(Schema schema) {
        int pos = schema.indexOf(column);
        DataType type = schema.getColumns().get(pos).getType();
        Object literal = prepare(type, value);
        Operator op = operator;

        return row -> {
            Object actual = row.get(pos);
            if (actual == null || literal == null) return false;  // как в SQL: сравнение с NULL ложно
            return op.test(compare(actual, literal));
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
