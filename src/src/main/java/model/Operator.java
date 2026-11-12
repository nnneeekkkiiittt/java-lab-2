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


}
