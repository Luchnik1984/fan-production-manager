package com.fanproduction.template.formula.parser;

/**
 * Токен DSL формул.
 *
 * @param type        тип токена
 * @param text        исходный текст токена (для отладки)
 * @param position    позиция в исходной строке (для сообщений об ошибках)
 * @param stringValue значение для STRING-токенов
 * @param numberValue значение для NUMBER-токенов
 */
public record Token(
        TokenType type,
        String text,
        int position,
        String stringValue,
        Number numberValue
) {

    public static Token string(String value, int position) {
        return new Token(TokenType.STRING, "'" + value + "'", position, value, null);
    }

    public static Token number(Number value, int position) {
        return new Token(TokenType.NUMBER, String.valueOf(value), position, null, value);
    }

    public static Token identifier(String value, int position) {
        return new Token(TokenType.IDENTIFIER, value, position, null, null);
    }

    public static Token symbol(TokenType type, String text, int position) {
        return new Token(type, text, position, null, null);
    }

    public static Token eof(int position) {
        return new Token(TokenType.EOF, "<EOF>", position, null, null);
    }
    
}