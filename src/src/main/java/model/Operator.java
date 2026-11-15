package model;

/** Перечисляет операторы сравнения, поддерживаемые в предложении WHERE. */
public enum Operator {
    /** Равенство двух значений ({@code =}). */
    EQUAL("="),
    /** Неравенство двух значений ({@code !=} или {@code <>}). */
    NOT_EQUAL("!="),
    /** Сравнение «меньше» ({@code <}). */
    LESS_THAN("<"),
    /** Сравнение «больше» ({@code >}). */
    GREATER_THAN(">"),
    /** Сравнение «меньше или равно» ({@code <=}). */
    LESS_OR_EQUAL("<="),
    /** Сравнение «больше или равно» ({@code >=}). */
    GREATER_OR_EQUAL(">=");
    private final String symbol;

    Operator(String symbol) {
        this.symbol = symbol;
    }

    /** Возвращает текстовый символ оператора, используемый в SQL-подобном запросе.
     * @return SQL-символ оператора.
     */
    public String getSymbol() {
        return symbol;
    }

    /** Преобразует текстовый оператор в значение перечисления.
     * @param s символ оператора.
     * @return оператор или {@code null}, если он не поддерживается.
     */
    public static Operator ToOperatorFromSql(String s) {
        // почти во всех субд <> == !=
        if (s.equals("<>")) return NOT_EQUAL;
        for (Operator op : values()) {
            if (op.symbol.equals(s)) return op;
        }
        return null;
    }

    /** Сравнивает фактическое значение с литералом.
     * @param actual значение из строки.
     * @param literal значение из условия.
     * @return результат сравнения.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public boolean compare(Object actual, Object literal) {
        if (actual == null || literal == null) return false;

        boolean numbers = actual instanceof Number && literal instanceof Number;

        switch (this) {
            case EQUAL:
                if (numbers) return ((Number) actual).doubleValue() == ((Number) literal).doubleValue();
                return actual.equals(literal);

            case NOT_EQUAL:
                if (numbers) return ((Number) actual).doubleValue() != ((Number) literal).doubleValue();
                return !actual.equals(literal);

            case LESS_THAN:
                if (numbers) return ((Number) actual).doubleValue() < ((Number) literal).doubleValue();
                return ((Comparable) actual).compareTo(literal) < 0;

            case GREATER_THAN:
                if (numbers) return ((Number) actual).doubleValue() > ((Number) literal).doubleValue();
                return ((Comparable) actual).compareTo(literal) > 0;

            case LESS_OR_EQUAL:
                if (numbers) return ((Number) actual).doubleValue() <= ((Number) literal).doubleValue();
                return ((Comparable) actual).compareTo(literal) <= 0;

            case GREATER_OR_EQUAL:
                if (numbers) return ((Number) actual).doubleValue() >= ((Number) literal).doubleValue();
                return ((Comparable) actual).compareTo(literal) >= 0;

            default:
                return false;
        }
    }
}
