package io.github.energyiot.data.access.cleaning;

import java.math.BigDecimal;
import java.math.MathContext;

public class FormulaEvaluator {

    private static final MathContext MATH_CONTEXT = MathContext.DECIMAL64;

    public BigDecimal evaluate(String formula, BigDecimal x) {
        String expression = formula == null || formula.trim().isEmpty() ? "x" : formula.trim();
        if (!expression.matches("[xX0-9+\\-*/().\\s]+")) {
            throw new IllegalArgumentException("formula only supports x, numbers, +, -, *, / and parentheses");
        }
        Parser parser = new Parser(expression, x);
        BigDecimal value = parser.parseExpression();
        parser.requireEnd();
        return value.stripTrailingZeros();
    }

    private static class Parser {
        private final String expression;
        private final BigDecimal x;
        private int position;

        Parser(String expression, BigDecimal x) {
            this.expression = expression;
            this.x = x;
        }

        BigDecimal parseExpression() {
            BigDecimal value = parseTerm();
            while (true) {
                skipWhitespace();
                if (match('+')) {
                    value = value.add(parseTerm(), MATH_CONTEXT);
                } else if (match('-')) {
                    value = value.subtract(parseTerm(), MATH_CONTEXT);
                } else {
                    return value;
                }
            }
        }

        private BigDecimal parseTerm() {
            BigDecimal value = parseFactor();
            while (true) {
                skipWhitespace();
                if (match('*')) {
                    value = value.multiply(parseFactor(), MATH_CONTEXT);
                } else if (match('/')) {
                    value = value.divide(parseFactor(), MATH_CONTEXT);
                } else {
                    return value;
                }
            }
        }

        private BigDecimal parseFactor() {
            skipWhitespace();
            if (match('+')) {
                return parseFactor();
            }
            if (match('-')) {
                return parseFactor().negate(MATH_CONTEXT);
            }
            if (match('(')) {
                BigDecimal value = parseExpression();
                if (!match(')')) {
                    throw new IllegalArgumentException("missing closing parenthesis in formula");
                }
                return value;
            }
            if (peekX()) {
                position++;
                return x;
            }
            return parseNumber();
        }

        private BigDecimal parseNumber() {
            skipWhitespace();
            int start = position;
            while (position < expression.length()) {
                char ch = expression.charAt(position);
                if ((ch >= '0' && ch <= '9') || ch == '.') {
                    position++;
                } else {
                    break;
                }
            }
            if (start == position) {
                throw new IllegalArgumentException("unexpected token in formula at position " + position);
            }
            return new BigDecimal(expression.substring(start, position), MATH_CONTEXT);
        }

        private void requireEnd() {
            skipWhitespace();
            if (position != expression.length()) {
                throw new IllegalArgumentException("unexpected token in formula at position " + position);
            }
        }

        private boolean match(char expected) {
            skipWhitespace();
            if (position < expression.length() && expression.charAt(position) == expected) {
                position++;
                return true;
            }
            return false;
        }

        private boolean peekX() {
            return position < expression.length()
                    && (expression.charAt(position) == 'x' || expression.charAt(position) == 'X');
        }

        private void skipWhitespace() {
            while (position < expression.length() && Character.isWhitespace(expression.charAt(position))) {
                position++;
            }
        }
    }
}
