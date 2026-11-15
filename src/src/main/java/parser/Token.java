package parser;

/** Один токен запроса с позицией в исходной строке (с нуля). */
public final class Token {
    /** Категория распознанного токена. */
    private final TokenType type;
    /** Текст токена в исходном запросе. */
    private final String text;
    /** Распознанное значение: {@link Long} или {@link Double} для числа, иначе {@code null}. */
    private final Object value;
    /** Позиция начала токена в исходной строке, отсчитываемая с нуля. */
    private final int position;

    /** Создаёт токен с типом, исходным текстом, значением и позицией.
     * @param type категория токена.
     * @param text исходный фрагмент запроса.
     * @param value распознанное значение токена или {@code null}.
     * @param position позиция начала токена в исходной строке.
     */
    public Token(TokenType type, String text, Object value, int position) {
        this.type = type;
        this.text = text;
        this.value = value;
        this.position = position;
    }

    /** Возвращает категорию токена.
     * @return тип токена.
     */
    public TokenType getType() { return type; }

    /** Возвращает исходный текст токена.
     * @return текст токена.
     */
    public String getText()    { return text; }

    /** Возвращает распознанное значение токена.
     * @return значение токена или {@code null}.
     */
    public Object getValue()   { return value; }

    /** Возвращает позицию начала токена в запросе.
     * @return позиция, отсчитываемая с нуля.
     */
    public int getPosition()   { return position; }

    @Override
    public String toString() {
        return type + "(" + text + ")";
    }
}
