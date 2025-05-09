/**
 * MicroScript — The programming language
 * Copyright (c) 2024-2025 Cyril John Magayaga
 * 
 * It was originally written in Java programming language.
 */
package com.magayaga.microscript;

import java.util.Stack;
import java.util.ArrayList;
import java.util.List;

public class ExpressionEvaluator {
    private final String expression;
    private final Environment environment;
    private int pos = -1;
    private int ch;

    public ExpressionEvaluator(String expression, Environment environment) {
        this.expression = expression;
        this.environment = environment;
    }

    public Object parse() {
        nextChar();
        Object x = parseTernary();
        if (pos < expression.length()) throw new RuntimeException("Unexpected: " + (char) ch);
        return x;
    }

    private Object parseTernary() {
        Object condition = parseComparison();
        skipWhitespace();
        
        // Look ahead for ? without consuming it
        if (ch == '?') {
            nextChar(); // consume ?
            skipWhitespace();
            
            Object trueValue = parseExpression();
            skipWhitespace();
            
            if (ch != ':') {
                throw new RuntimeException("Missing ':' in ternary expression at position " + pos);
            }
            nextChar(); // consume :
            skipWhitespace();
            
            Object falseValue = parseExpression();
            
            // Evaluate the ternary expression with proper condition checking
            double condValue = objectToDouble(condition);
            return Math.abs(condValue) > 0.0001 ? trueValue : falseValue;
        }
        
        return condition;
    }

    // Support comparison operators and spaceship operator
    private Object parseComparison() {
        Object x = parseExpression();
        double xValue = objectToDouble(x);
        
        // Handle spaceship operator first
        if (eat('<')) {
            if (eat('=')) {
                if (eat('>')) { // <=> operator
                    Object rightObj = parseExpression();
                    double right = objectToDouble(rightObj);
                    return (double)Double.compare(xValue, right);
                }
            }
        }
        
        // Reset position if it wasn't a spaceship operator
        if (eat('<')) {
            if (eat('=')) {
                Object rightObj = parseExpression();
                double right = objectToDouble(rightObj);
                return xValue <= right ? 1.0 : 0.0;
            }
            Object rightObj = parseExpression();
            double right = objectToDouble(rightObj);
            return xValue < right ? 1.0 : 0.0;
        } else if (eat('>')) {
            if (eat('=')) {
                Object rightObj = parseExpression();
                double right = objectToDouble(rightObj);
                return xValue >= right ? 1.0 : 0.0;
            }
            Object rightObj = parseExpression();
            double right = objectToDouble(rightObj);
            return xValue > right ? 1.0 : 0.0;
        } else if (eat('=')) {
            if (eat('=')) {
                // Equal ==
                Object rightObj = parseExpression();
                double right = objectToDouble(rightObj);
                return xValue == right ? 1.0 : 0.0;
            }
            throw new RuntimeException("Unexpected '='");
        } else if (eat('!')) {
            if (eat('=')) {
                // Not equal !=
                Object rightObj = parseExpression();
                double right = objectToDouble(rightObj);
                return xValue != right ? 1.0 : 0.0;
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

    private Object parseExpression() {
        Object x = parseTerm();
        double xValue;
        for (;;) {
            if (eat('+')) {
                Object termObj = parseTerm();
                xValue = objectToDouble(x);
                double termValue = objectToDouble(termObj);
                x = xValue + termValue; // addition
            }
            else if (eat('-')) {
                Object termObj = parseTerm();
                xValue = objectToDouble(x);
                double termValue = objectToDouble(termObj);
                x = xValue - termValue; // subtraction
            }
            else return x;
        }
    }

    private Object parseTerm() {
        Object x = parseFactor();
        double xValue;
        for (;;) {
            if (eat('*')) {
                Object factorObj = parseFactor();
                xValue = objectToDouble(x);
                double factorValue = objectToDouble(factorObj);
                x = xValue * factorValue; // multiplication
            }
            else if (eat('/')) {
                Object factorObj = parseFactor();
                xValue = objectToDouble(x);
                double factorValue = objectToDouble(factorObj);
                x = xValue / factorValue; // division
            }
            else return x;
        }
    }

    private double objectToDouble(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        } else {
            throw new RuntimeException("Cannot convert " + obj + " to a number");
        }
    }

    private Object parseFactor() {
        if (eat('+')) return parseFactor(); // unary plus
        if (eat('-')) {
            Object factor = parseFactor();
            return -objectToDouble(factor); // unary minus
        }

        Object x;
        int startPos = this.pos;
        if (eat('(')) { // parentheses
            x = parseExpression();
            eat(')');
        }
        
        else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
            while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
            x = Double.parseDouble(expression.substring(startPos, this.pos));
        }
        
        else if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_') { // functions or variables
            StringBuilder identifier = new StringBuilder();
            while ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9') || ch == '_') {
                identifier.append((char)ch);
                nextChar();
            }
            
            // Look ahead for module operator ::
            if (pos < expression.length() && ch == ':') {
                nextChar(); // consume first :
                if (ch == ':') {
                    nextChar(); // consume second :
                    
                    // Parse function name after ::
                    StringBuilder functionName = new StringBuilder();
                    while ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || 
                           (ch >= '0' && ch <= '9') || ch == '_') {
                        functionName.append((char)ch);
                        nextChar();
                    }
                    String fullName = identifier + "::" + functionName;
                    
                    // Handle module function call with proper argument parsing
                    if (eat('(')) {
                        List<Object> args = new ArrayList<>();
                        if (!eat(')')) {  // If not empty arguments
                            while (true) {
                                args.add(parseExpression());
                                if (eat(')')) break;
                                if (!eat(',')) throw new RuntimeException("Expected ',' or ')' in argument list");
                            }
                        }
                        
                        Object func = environment.getVariable(fullName.toString());
                        if (func instanceof Import.FunctionInterface) {
                            Object[] argsArray = new Object[args.size()];
                            for (int i = 0; i < args.size(); i++) {
                                argsArray[i] = args.get(i);
                            }
                            return ((Import.FunctionInterface)func).call(argsArray);
                        }
                        throw new RuntimeException("Unknown module function: " + fullName);
                    }
                    throw new RuntimeException("Expected ( after module function " + fullName);
                } else {
                    pos--; // Go back if second : is not found
                    ch = expression.charAt(pos);
                }
            }
            
            // Handle regular functions or variables
            String func = identifier.toString();
            if (eat('(')) {
                List<Object> args = new ArrayList<>();
                if (!eat(')')) {  // If not empty arguments
                    while (true) {
                        args.add(parseExpression());
                        if (eat(')')) break;
                        if (!eat(',')) throw new RuntimeException("Expected ',' or ')' in argument list");
                    }
                }
                // Handle function call here - replace with actual function handling
                throw new RuntimeException("Function call not implemented for: " + func);
            }
            else {
                Object varValue = environment.getVariable(func);
                if (varValue != null) {
                    return varValue;
                } else {
                    throw new RuntimeException("Undefined variable: " + func);
                }
            }
        }
        else {
            throw new RuntimeException("Unexpected character: " + (char)ch);
        }

        if (eat('^')) {
            Object factorObj = parseFactor();
            double xValue = objectToDouble(x);
            double factorValue = objectToDouble(factorObj);
            x = Math.pow(xValue, factorValue); // exponentiation
        }
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
