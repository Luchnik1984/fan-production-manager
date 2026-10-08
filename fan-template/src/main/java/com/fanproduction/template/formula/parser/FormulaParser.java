package com.fanproduction.template.formula.parser;

import com.fanproduction.template.formula.ast.Expression;
import com.fanproduction.template.formula.ast.FieldExpression;
import com.fanproduction.template.formula.ast.FunctionExpression;
import com.fanproduction.template.formula.ast.LiteralExpression;
import com.fanproduction.template.formula.ast.MapExpression;
import com.fanproduction.template.formula.exception.FormulaException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Парсер DSL формул.
 * <p>
 * Рекурсивный спуск. Строит AST из строки.
 * <p>
 * Ограничения:
 * <ul>
 *   <li>Длина формулы — не более {@value #MAX_FORMULA_LENGTH} символов.</li>
 *   <li>Глубина AST — не более {@value #MAX_DEPTH} уровней.</li>
 *   <li>Строковые литералы — без escape-последовательностей.</li>
 * </ul>
 */
public class FormulaParser {

    /** Максимальная длина формулы. */
    public static final int MAX_FORMULA_LENGTH = 5000;

    /** Максимальная глубина AST. */
    public static final int MAX_DEPTH = 50;

    private final List<Token> tokens;
    private int position;
    private int depth;

    public FormulaParser(String input) {
        if (input == null || input.isBlank()) {
            throw new FormulaException("Формула не может быть пустой");
        }
        if (input.length() > MAX_FORMULA_LENGTH) {
            throw new FormulaException(
                    "Формула слишком длинная: " + input.length()
                            + " символов (макс. " + MAX_FORMULA_LENGTH + ")");
        }

        this.tokens = new FormulaTokenizer(input).tokenize();
        this.position = 0;
        this.depth = 0;
    }

    /**
     * Разобрать формулу.
     * <p>
     * После разбора ожидается конец ввода. Если остались токены —
     * выбрасывается {@link FormulaException}.
     */
    public Expression parse() {
        Expression result = parseExpression();

        if (current().type() != TokenType.EOF) {
            throw new FormulaException(
                    "Неожиданный токен после выражения: " + current()
                            + " (позиция " + current().position() + ")");
        }

        return result;
    }

    // ==========================================================
    // ОСНОВНОЙ ЦИКЛ РАЗБОРА
    // ==========================================================

    private Expression parseExpression() {
        depth++;
        if (depth > MAX_DEPTH) {
            throw new FormulaException(
                    "Слишком глубокая вложенность выражений (макс. " + MAX_DEPTH + ")");
        }

        try {
            Token token = current();

            return switch (token.type()) {
                case STRING -> {
                    advance();
                    yield new LiteralExpression(token.stringValue());
                }
                case NUMBER -> {
                    advance();
                    yield new LiteralExpression(token.numberValue());
                }
                case IDENTIFIER -> parseIdentifierOrKeyword();
                case LBRACE -> parseMap();
                default -> throw new FormulaException(
                        "Неожиданный токен: " + token
                                + " (позиция " + token.position() + ")");
            };
        } finally {
            depth--;
        }
    }

    private Expression parseIdentifierOrKeyword() {
        Token ident = current();
        String name = ident.text();
        String upperName = name.toUpperCase();

        // FIELD — специальный случай: FIELD('name')
        if ("FIELD".equals(upperName)) {
            advance();
            expect(TokenType.LPAREN, "Ожидалась '(' после FIELD");
            Token fieldToken = expect(TokenType.STRING, "Ожидалось имя поля в кавычках");
            expect(TokenType.RPAREN, "Ожидалась ')' после имени поля");
            return new FieldExpression(fieldToken.stringValue());
        }

        // TRUE / FALSE без скобок — литералы
        if ("TRUE".equals(upperName) && peekType() != TokenType.LPAREN) {
            advance();
            return new LiteralExpression(true);
        }
        if ("FALSE".equals(upperName) && peekType() != TokenType.LPAREN) {
            advance();
            return new LiteralExpression(false);
        }

        // Обычная функция: NAME(args...)
        advance();
        expect(TokenType.LPAREN, "Ожидалась '(' после '" + name + "'");

        List<Expression> arguments = new ArrayList<>();
        if (current().type() != TokenType.RPAREN) {
            arguments.add(parseExpression());
            while (current().type() == TokenType.COMMA) {
                advance();
                arguments.add(parseExpression());
            }
        }

        expect(TokenType.RPAREN, "Ожидалась ')' после аргументов '" + name + "'");

        return new FunctionExpression(upperName, List.copyOf(arguments));
    }

    private Expression parseMap() {
        expect(TokenType.LBRACE, "Ожидалась '{'");

        Map<Object, Expression> entries = new LinkedHashMap<>();

        if (current().type() != TokenType.RBRACE) {
            parseMapEntry(entries);
            while (current().type() == TokenType.COMMA) {
                advance();
                parseMapEntry(entries);
            }
        }

        expect(TokenType.RBRACE, "Ожидалась '}'");
        return new MapExpression(Map.copyOf(entries));
    }

    private void parseMapEntry(Map<Object, Expression> entries) {
        Token keyToken = current();
        Object key;

        switch (keyToken.type()) {
            case STRING -> {
                key = keyToken.stringValue();
                advance();
            }
            case NUMBER -> {
                key = keyToken.numberValue();
                advance();
            }
            case IDENTIFIER -> {
                key = keyToken.text();
                advance();
            }
            default -> throw new FormulaException(
                    "Некорректный ключ в MAP: " + keyToken
                            + " (позиция " + keyToken.position() + ")");
        }

        expect(TokenType.COLON, "Ожидался ':' после ключа");
        Expression value = parseExpression();

        if (entries.containsKey(key)) {
            throw new FormulaException("Дублирующийся ключ в MAP: " + key);
        }

        entries.put(key, value);
    }

    // ==========================================================
    // ВСПОМОГАТЕЛЬНЫЕ
    // ==========================================================

    private Token current() {
        return tokens.get(position);
    }

    private TokenType peekType() {
        int next = position + 1;
        if (next >= tokens.size()) {
            return TokenType.EOF;
        }
        return tokens.get(next).type();
    }

    private Token advance() {
        Token token = tokens.get(position);
        if (token.type() != TokenType.EOF) {
            position++;
        }
        return token;
    }

    private Token expect(TokenType expected, String errorMessage) {
        Token token = current();
        if (token.type() != expected) {
            throw new FormulaException(
                    errorMessage + " — получен " + token
                            + " (позиция " + token.position() + ")");
        }
        advance();
        return token;
    }
}
