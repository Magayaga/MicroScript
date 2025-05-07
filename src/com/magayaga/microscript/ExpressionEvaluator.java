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
        skipWhitespace();
        
        // Look ahead for ? without consuming it
        if (ch == '?') {
            nextChar(); // consume ?
            skipWhitespace();
            
            double trueValue = parseExpression();
            skipWhitespace();
            
            if (ch != ':') {
                throw new RuntimeException("Missing ':' in ternary expression at position " + pos);
            }
            nextChar(); // consume :
            skipWhitespace();
            
            double falseValue = parseExpression();
            
            // Evaluate the ternary expression with proper condition checking
            return Math.abs(condition) > 0.0001 ? trueValue : falseValue;
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
        skipWhitespace(); // Use skipWhitespace instead of while loop
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
        
        else if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) { // functions or variables
            StringBuilder identifier = new StringBuilder();
            while ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) {
                identifier.append((char)ch);
                nextChar();
            }
            
            // Look ahead for module operator ::
            if (pos + 1 < expression.length() && 
                expression.charAt(pos) == ':' && 
                expression.charAt(pos + 1) == ':') {
                
                nextChar(); // consume first :
                nextChar(); // consume second :
                
                // Parse function name after ::
                StringBuilder functionName = new StringBuilder();
                while ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')) {
                    functionName.append((char)ch);
                    nextChar();
                }
                String fullName = identifier + "::" + functionName;
                
                if (eat('(')) {
                    x = parseExpression();
                    if (!eat(')')) {
                        throw new RuntimeException("Missing closing parenthesis for module function " + fullName);
                    }
                    Object func = environment.getVariable(fullName.toString());
                    if (func instanceof Import.FunctionInterface) {
                        return ((Number)((Import.FunctionInterface)func).call(new Object[]{x})).doubleValue();
                    }
                    throw new RuntimeException("Unknown module function: " + fullName);
                }
                throw new RuntimeException("Expected ( after module function " + fullName);
            }
            
            // Handle regular functions or variables
            String func = identifier.toString();
            if (eat('(')) {
                x = parseExpression();
                if (!eat(')')) {
                    throw new RuntimeException("Missing closing parenthesis for function " + func);
                }
                // Handle function call here
                x = 0; // Replace with actual function handling
            }
            else {
                Object varValue = environment.getVariable(func);
                if (varValue instanceof Number) {
                    x = ((Number) varValue).doubleValue();
                } else if (varValue != null) {
                    throw new RuntimeException("Variable '" + func + "' is not a number");
                } else {
                    throw new RuntimeException("Undefined variable: " + func);
                }
            }
        }
        else {
            throw new RuntimeException("Unexpected character: " + (char)ch);
        }

        if (eat('^')) x = Math.pow(x, parseFactor()); // exponentiation
        return x;
    }

    // Add this helper method to skip whitespace
    private void skipWhitespace() {
        while (pos < expression.length() && 
               (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n')) {
            nextChar();
        }
    }
}
