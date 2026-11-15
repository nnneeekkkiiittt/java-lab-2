package parser;

import exception.ParseException;

import java.util.ArrayList;
import java.util.List;

/**
 * Разбивает строку запроса на токены. Строки в одинарных кавычках могут содержать пробелы
 * и запятые, внутри строки кавычка удваивается: {@code 'O''Neil'}.
 */
public final class Tokenizer {

    private Tokenizer() {
    }

    public static List<Token> tokenize(String sql) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        int n = sql.length();

        while (i < n) {
            char c = sql.charAt(i);

            if (Character.isWhitespace(c)) {
                i++;

            } else if (isWordStart(c)) {
                int start = i;
                while (i < n && isWordPart(sql.charAt(i))) i++;
                tokens.add(new Token(TokenType.WORD, sql.substring(start, i), null, start));

            } else if (Character.isDigit(c) || (c == '-' && i + 1 < n && Character.isDigit(sql.charAt(i + 1)))) {
                int start = i;
                i++;
                while (i < n && Character.isDigit(sql.charAt(i))) i++;
                boolean fractional = false;
                if (i + 1 < n && sql.charAt(i) == '.' && Character.isDigit(sql.charAt(i + 1))) {
                    fractional = true;
                    i++;
                    while (i < n && Character.isDigit(sql.charAt(i))) i++;
                }
                String text = sql.substring(start, i);
                tokens.add(new Token(TokenType.NUMBER, text, parseNumber(text, fractional, start), start));

            } else if (c == '\'') {
                int start = i;
                i++;
                StringBuilder value = new StringBuilder();
                boolean closed = false;
                while (i < n) {
                    char d = sql.charAt(i);
                    if (d == '\'') {
                        if (i + 1 < n && sql.charAt(i + 1) == '\'') {   // '' внутри строки
                            value.append('\'');
                            i += 2;
                        } else {
                            i++;
                            closed = true;
                            break;
                        }
                    } else {
                        value.append(d);
                        i++;
                    }
                }
                if (!closed) {
                    throw new ParseException("Незакрытая строка, начало на позиции " + start);
                }
                tokens.add(new Token(TokenType.STRING, value.toString(), null, start));

            } else {
                int start = i;
                String symbol = symbolAt(sql, i);
                if (symbol == null) {
                    throw new ParseException("Неожиданный символ '" + c + "' на позиции " + i);
                }
                i += symbol.length();
                tokens.add(new Token(TokenType.SYMBOL, symbol, null, start));
            }
        }
        tokens.add(new Token(TokenType.END, "", null, n));
        return tokens;
    }

    private static boolean isWordStart(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private static boolean isWordPart(char c) {
        return isWordStart(c) || (c >= '0' && c <= '9');
    }

    private static Object parseNumber(String text, boolean fractional, int position) {
        try {
            return fractional ? (Object) Double.parseDouble(text) : (Object) Long.parseLong(text);
        } catch (NumberFormatException e) {
            throw new ParseException("Слишком большое число '" + text + "' на позиции " + position);
        }
    }

    /** Символ или оператор, начинающийся в позиции i, либо null. Двухсимвольные проверяются первыми. */
    private static String symbolAt(String sql, int i) {
        String two = i + 1 < sql.length() ? sql.substring(i, i + 2) : "";
        if (two.equals("<=") || two.equals(">=") || two.equals("!=") || two.equals("<>")) {
            return two;
        }
        char c = sql.charAt(i);
        switch (c) {
            case '(': case ')': case ',': case ';': case '*':
            case '=': case '<': case '>':
                return String.valueOf(c);
            default:
                return null;
        }
    }
}
