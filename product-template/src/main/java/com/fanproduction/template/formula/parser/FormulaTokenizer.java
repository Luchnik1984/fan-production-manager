package com.fanproduction.template.formula.parser;

import com.fanproduction.template.formula.exception.FormulaException;

import java.util.ArrayList;
import java.util.List;

/**
 * Токенайзер DSL формул.
 * <p>
 * Разбирает входную строку на токены: строковые литералы, числа,
 * идентификаторы, символы-разделители.
 * <p>
 * Пробелы, переводы строк и табы игнорируются.
 * Строки обрамляются одинарными кавычками. Escape-последовательности
 * на этом этапе не поддерживаются.
 */
public class FormulaTokenizer {

    private final String input;
    private int position;

    public FormulaTokenizer(String input) {
        this.input = input;
        this.position = 0;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();

        while (position < input.length()) {
            char c = input.charAt(position);

            // Пропуск пробельных символов
            if (Character.isWhitespace(c)) {
                position++;
                continue;
            }

            // Строковый литерал
            if (c == '\'') {
                tokens.add(readString());
                continue;
            }

            // Число
            if (Character.isDigit(c)) {
                tokens.add(readNumber());
                continue;
            }

            // Идентификатор
            if (Character.isLetter(c) || c == '_') {
                tokens.add(readIdentifier());
                continue;
            }

            // Символы
            switch (c) {
                case '(' -> {
                    tokens.add(Token.symbol(TokenType.LPAREN, "(", position));
                    position++;
                }
                case ')' -> {
                    tokens.add(Token.symbol(TokenType.RPAREN, ")", position));
                    position++;
                }
                case '{' -> {
                    tokens.add(Token.symbol(TokenType.LBRACE, "{", position));
                    position++;
                }
                case '}' -> {
                    tokens.add(Token.symbol(TokenType.RBRACE, "}", position));
                    position++;
                }
                case ',' -> {
                    tokens.add(Token.symbol(TokenType.COMMA, ",", position));
                    position++;
                }
                case ':' -> {
                    tokens.add(Token.symbol(TokenType.COLON, ":", position));
                    position++;
                }
                default -> throw new FormulaException(
                        "Недопустимый символ '" + c + "' в позиции " + position);
            }
        }

        tokens.add(Token.eof(position));
        return tokens;
    }

    private Token readString() {
        int start = position;
        position++; // пропускаем открывающую кавычку

        StringBuilder sb = new StringBuilder();
        while (position < input.length() && input.charAt(position) != '\'') {
            sb.append(input.charAt(position));
            position++;
        }

        if (position >= input.length()) {
            throw new FormulaException(
                    "Незакрытая строка в позиции " + start);
        }

        position++; // пропускаем закрывающую кавычку
        return Token.string(sb.toString(), start);
    }

    private Token readNumber() {
        int start = position;
        StringBuilder sb = new StringBuilder();

        while (position < input.length() && Character.isDigit(input.charAt(position))) {
            sb.append(input.charAt(position));
            position++;
        }

        // Дробная часть
        if (position < input.length() && input.charAt(position) == '.') {
            sb.append('.');
            position++;
            while (position < input.length() && Character.isDigit(input.charAt(position))) {
                sb.append(input.charAt(position));
                position++;
            }
        }

        String text = sb.toString();
        try {
            if (text.contains(".")) {
                return Token.number(Double.parseDouble(text), start);
            } else {
                // Пробуем int, если влезает; иначе — long
                long value = Long.parseLong(text);
                if (value >= Integer.MIN_VALUE && value <= Integer.MAX_VALUE) {
                    return Token.number((int) value, start);
                }
                return Token.number(value, start);
            }
        } catch (NumberFormatException e) {
            throw new FormulaException(
                    "Некорректное число '" + text + "' в позиции " + start);
        }
    }

    private Token readIdentifier() {
        int start = position;
        StringBuilder sb = new StringBuilder();

        while (position < input.length()) {
            char c = input.charAt(position);
            if (Character.isLetterOrDigit(c) || c == '_') {
                sb.append(c);
                position++;
            } else {
                break;
            }
        }

        return Token.identifier(sb.toString(), start);
    }
}
