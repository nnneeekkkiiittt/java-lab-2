package parser;

/** Один токен запроса с позицией в исходной строке (с нуля). */
public final class Token {
    private final TokenType type;
    private final String text;
    private final Object value;   // Long или Double для NUMBER, иначе null
    private final int position;

    public Token(TokenType type, String text, Object value, int position) {
        this.type = type;
        this.text = text;
        this.value = value;
        this.position = position;
    }

    public TokenType getType() { return type; }
    public String getText()    { return text; }
    public Object getValue()   { return value; }
    public int getPosition()   { return position; }

    @Override
    public String toString() {
        return type + "(" + text + ")";
    }
}
