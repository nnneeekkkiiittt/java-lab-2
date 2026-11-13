package model;

public enum Operator {
    EQUAL("="), NOT_EQUAL("!="), LESS_THAN("<"), GREATER_THAN(">"), LESS_OR_EQUAL("<="), GREATER_OR_EQUAL(">=");
    private final String symbol;

    Operator(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public static Operator ToOperatorFromSql(String s) {
        // почти во всех субд <> == !=
        if (s.equals("<>")) return NOT_EQUAL;
        for (Operator op : values()) {
            if (op.symbol.equals(s)) return op;
        }
        return null;
    }

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
