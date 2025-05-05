/**
 * MicroScript — The programming language
 * Copyright (c) 2024-2025 Cyril John Magayaga
 * 
 * It was originally written in Java programming language.
 */
package com.magayaga.microscript;

import java.util.Stack;

public class ExpressionEvaluator {
    private final String expression;
    private final Environment environment;
    private int pos = -1;
    private int ch;

    public ExpressionEvaluator(String expression, Environment environment) {
        this.expression = expression;
        this.environment = environment;
    }

    public double parse() {
        nextChar();
        double x = parseTernary();
        if (pos < expression.length()) throw new RuntimeException("Unexpected: " + (char) ch);
        return x;
    }

    private double parseTernary() {
        double condition = parseComparison();
        if (eat('?')) {
            double trueValue = parseExpression();  // Parse true expression
            if (!eat(':')) {
                throw new RuntimeException("Expected ':' in ternary operator");
            }
            double falseValue = parseExpression(); // Parse false expression
            return condition != 0.0 ? trueValue : falseValue;
        }
        return condition;
    }

    // Support comparison operators and spaceship operator
    private double parseComparison() {
        double x = parseExpression();
        
        // Handle spaceship operator first
        if (eat('<')) {
            if (eat('=')) {
                if (eat('>')) { // <=> operator
                    double right = parseExpression();
                    return Double.compare(x, right);
                }
            }
        }
        
        // Reset position if it wasn't a spaceship operator
        if (eat('<')) {
            if (eat('=')) {
                double right = parseExpression();
                return x <= right ? 1.0 : 0.0;
            }
            double right = parseExpression();
            return x < right ? 1.0 : 0.0;
        } else if (eat('>')) {
            if (eat('=')) {
                double right = parseExpression();
                return x >= right ? 1.0 : 0.0;
            }
            double right = parseExpression();
            return x > right ? 1.0 : 0.0;
        } else if (eat('=')) {
            if (eat('=')) {
                // Equal ==
                double right = parseExpression();
                return x == right ? 1.0 : 0.0;
            }
            throw new RuntimeException("Unexpected '='");
        } else if (eat('!')) {
            if (eat('=')) {
                // Not equal !=
                double right = parseExpression();
                return x != right ? 1.0 : 0.0;
            }
            throw new RuntimeException("Unexpected '!'");
        }
        return x;
    }

    private void nextChar() {
        ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
    }

    private boolean eat(int charToEat) {
        while (ch == ' ') nextChar();
        if (ch == charToEat) {
            nextChar();
            return true;
        }
        return false;
    }

    private double parseExpression() {
        double x = parseTerm();
        for (;;) {
            if      (eat('+')) x += parseTerm(); // addition
            else if (eat('-')) x -= parseTerm(); // subtraction
            else return x;
        }
    }

    private double parseTerm() {
        double x = parseFactor();
        for (;;) {
            if      (eat('*')) x *= parseFactor(); // multiplication
            else if (eat('/')) x /= parseFactor(); // division
            else return x;
        }
    }

    private double parseFactor() {
        if (eat('+')) return parseFactor(); // unary plus
        if (eat('-')) return -parseFactor(); // unary minus

        double x;
        int startPos = this.pos;
        if (eat('(')) { // parentheses
            x = parseExpression();
            eat(')');
        }
        
        else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
            while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
            x = Double.parseDouble(expression.substring(startPos, this.pos));
        }
        
        else if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) { // functions or variables (now supports uppercase)
            while ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) nextChar();
            String func = expression.substring(startPos, this.pos);
            if (eat('(')) {
                x = parseExpression();
                eat(')');
            }
            else {
                Object varValue = environment.getVariable(func);
                if (varValue instanceof Number) {
                    x = ((Number) varValue).doubleValue();
                } else if (varValue != null) {
                    throw new RuntimeException("Variable '" + func + "' is not a number.");
                } else {
                    x = 0;
                }
            }
        }
        
        else {
            throw new RuntimeException("Unexpected: " + (char) ch);
        }

        if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation

        return x;
    }
}
