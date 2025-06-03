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
        skipWhitespace(); // Start by skipping initial whitespace
        
        // Check for increment/decrement statements first
        Object incrementResult = parseIncrementDecrement();
        if (incrementResult != null) {
            return incrementResult;
        }
        
        // Reset position for normal parsing
        pos = -1;
        nextChar();
        skipWhitespace();
        
        Object x = parseAssignment(); // Start with assignment (walrus operator)
        skipWhitespace(); // Skip any trailing whitespace
        if (pos < expression.length()) throw new RuntimeException("Unexpected: " + (char) ch);
        return x;
    }

    private Object parseIncrementDecrement() {
        // Save initial state
        int savedPos = pos;
        int savedCh = ch;
        
        // Parse variable name
        StringBuilder varName = new StringBuilder();
        while ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || 
               (ch >= '0' && ch <= '9') || ch == '_') {
            varName.append((char)ch);
            nextChar();
        }
        
        skipWhitespace();
        
        // Check for ++ or --
        if (ch == '+') {
            nextChar();
            if (ch == '+') {
                nextChar(); // consume second +
                skipWhitespace();
                
                // Check if we're at the end or semicolon
                if (pos >= expression.length() || ch == ';') {
                    // This is a post-increment: variable++
                    String variable = varName.toString();
                    Object currentValue = environment.getVariable(variable);
                    
                    if (currentValue == null) {
                        throw new RuntimeException("Undefined variable: " + variable);
                    }
                    
                    if (!(currentValue instanceof Number)) {
                        throw new RuntimeException("Cannot increment non-numeric variable: " + variable);
                    }
                    
                    double currentNum = ((Number) currentValue).doubleValue();
                    double newValue = currentNum + 1;
                    
                    // Store the new value
                    if (currentValue instanceof Integer) {
                        environment.setVariable(variable, (int) newValue);
                        return (int) currentNum; // Return original value for post-increment
                    } else {
                        environment.setVariable(variable, newValue);
                        return currentNum; // Return original value for post-increment
                    }
                }
            }
        } else if (ch == '-') {
            nextChar();
            if (ch == '-') {
                nextChar(); // consume second -
                skipWhitespace();
                
                // Check if we're at the end or semicolon
                if (pos >= expression.length() || ch == ';') {
                    // This is a post-decrement: variable--
                    String variable = varName.toString();
                    Object currentValue = environment.getVariable(variable);
                    
                    if (currentValue == null) {
                        throw new RuntimeException("Undefined variable: " + variable);
                    }
                    
                    if (!(currentValue instanceof Number)) {
                        throw new RuntimeException("Cannot decrement non-numeric variable: " + variable);
                    }
                    
                    double currentNum = ((Number) currentValue).doubleValue();
                    double newValue = currentNum - 1;
                    
                    // Store the new value
                    if (currentValue instanceof Integer) {
                        environment.setVariable(variable, (int) newValue);
                        return (int) currentNum; // Return original value for post-decrement
                    } else {
                        environment.setVariable(variable, newValue);
                        return currentNum; // Return original value for post-decrement
                    }
                }
            }
        }
        
        // Restore state if not increment/decrement
        pos = savedPos;
        ch = savedCh;
        return null;
    }

    private Object parseAssignment() {
        // Look ahead for assignment operators (:=, +=, -=, *=, /=)
        int savedPos = pos;
        int savedCh = ch;
        
        // Parse potential variable name
        StringBuilder varName = new StringBuilder();
        while ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || 
               (ch >= '0' && ch <= '9') || ch == '_') {
            varName.append((char)ch);
            nextChar();
        }
        
        skipWhitespace();
        
        // Check for assignment operators
        String assignmentOp = null;
        if (ch == ':') {
            nextChar();
            if (ch == '=') {
                assignmentOp = ":="; // walrus operator
                nextChar(); // consume =
                skipWhitespace();
            } else {
                // Revert back if not a walrus operator
                pos = savedPos;
                ch = savedCh;
            }
        } else if (ch == '+') {
            nextChar();
            if (ch == '=') {
                assignmentOp = "+="; // compound addition
                nextChar(); // consume =
                skipWhitespace();
            } else {
                // Revert back if not a compound assignment
                pos = savedPos;
                ch = savedCh;
            }
        } else if (ch == '-') {
            nextChar();
            if (ch == '=') {
                assignmentOp = "-="; // compound subtraction
                nextChar(); // consume =
                skipWhitespace();
            } else {
                // Revert back if not a compound assignment
                pos = savedPos;
                ch = savedCh;
            }
        } else if (ch == '*') {
            nextChar();
            if (ch == '=') {
                assignmentOp = "*="; // compound multiplication
                nextChar(); // consume =
                skipWhitespace();
            } else {
                // Revert back if not a compound assignment
                pos = savedPos;
                ch = savedCh;
            }
        } else if (ch == '/') {
            nextChar();
            if (ch == '=') {
                assignmentOp = "/="; // compound division
                nextChar(); // consume =
                skipWhitespace();
            } else {
                // Revert back if not a compound assignment
                pos = savedPos;
                ch = savedCh;
            }
        } else {
            // Revert back if not an assignment
            pos = savedPos;
            ch = savedCh;
        }
        
        if (assignmentOp != null) {
            String variable = varName.toString();
            Object rightValue = parseAssignment(); // Support chained assignments
            
            if (assignmentOp.equals(":=")) {
                // Walrus operator - simple assignment
                environment.setVariable(variable, rightValue);
                return rightValue;
            } else {
                // Compound assignment - need to get current value first
                Object currentValue = environment.getVariable(variable);
                if (currentValue == null) {
                    throw new RuntimeException("Undefined variable for compound assignment: " + variable);
                }
                
                double currentNum = objectToDouble(currentValue);
                double rightNum = objectToDouble(rightValue);
                double result;
                
                switch (assignmentOp) {
                    case "+=":
                        result = currentNum + rightNum;
                        break;
                    case "-=":
                        result = currentNum - rightNum;
                        break;
                    case "*=":
                        result = currentNum * rightNum;
                        break;
                    case "/=":
                        if (Math.abs(rightNum) < 0.0001) {
                            throw new RuntimeException("Division by zero in compound assignment");
                        }
                        result = currentNum / rightNum;
                        break;
                    default:
                        throw new RuntimeException("Unknown compound assignment operator: " + assignmentOp);
                }
                
                // Store the result, preserving the original type if it was an integer
                Object finalValue;
                if (currentValue instanceof Integer && result == Math.floor(result)) {
                    finalValue = (int) result;
                } else {
                    finalValue = result;
                }
                
                environment.setVariable(variable, finalValue);
                return finalValue;
            }
        }
        
        return parseTernary();
    }

    private Object parseTernary() {
        Object condition = parseComparison();
        skipWhitespace();
        
        // Look for ternary operator ?
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
            
            // Evaluate the ternary expression
            boolean condResult = isTruthy(condition);
            return condResult ? trueValue : falseValue;
        }
        
        return condition;
    }

    // Check if an object is truthy (non-zero for numbers, true for booleans)
    private boolean isTruthy(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof Boolean) {
            return (Boolean) obj;
        }
        if (obj instanceof Number) {
            return Math.abs(((Number) obj).doubleValue()) > 0.0001;
        }
        if (obj instanceof String) {
            return !((String) obj).isEmpty();
        }
        return true; // Any other object is considered truthy
    }

    // Support comparison operators and spaceship operator
    private Object parseComparison() {
        Object x = parseExpression();
        skipWhitespace();
        
        // Handle spaceship operator <=>
        if (ch == '<') {
            nextChar();
            if (ch == '=') {
                nextChar();
                if (ch == '>') { // Found <=> operator
                    nextChar();
                    skipWhitespace();
                    Object rightObj = parseExpression();
                    double left = objectToDouble(x);
                    double right = objectToDouble(rightObj);
                    return (double)Double.compare(left, right);
                } else {
                    // It's <= operator
                    skipWhitespace();
                    Object rightObj = parseExpression();
                    double left = objectToDouble(x);
                    double right = objectToDouble(rightObj);
                    return left <= right;
                }
            } else {
                // It's < operator
                skipWhitespace();
                Object rightObj = parseExpression();
                double left = objectToDouble(x);
                double right = objectToDouble(rightObj);
                return left < right;
            }
        } else if (ch == '>') {
            nextChar();
            if (ch == '=') {
                nextChar();
                skipWhitespace();
                Object rightObj = parseExpression();
                double left = objectToDouble(x);
                double right = objectToDouble(rightObj);
                return left >= right;
            } else {
                skipWhitespace();
                Object rightObj = parseExpression();
                double left = objectToDouble(x);
                double right = objectToDouble(rightObj);
                return left > right;
            }
        } else if (ch == '=') {
            nextChar();
            if (ch == '=') {
                nextChar();
                skipWhitespace();
                // Equal ==
                Object rightObj = parseExpression();
                double left = objectToDouble(x);
                double right = objectToDouble(rightObj);
                return Math.abs(left - right) < 0.0001;
            }
            throw new RuntimeException("Unexpected '=' at position " + pos + ". Did you mean '=='?");
        } else if (ch == '!') {
            nextChar();
            if (ch == '=') {
                nextChar();
                skipWhitespace();
                // Not equal !=
                Object rightObj = parseExpression();
                double left = objectToDouble(x);
                double right = objectToDouble(rightObj);
                return Math.abs(left - right) >= 0.0001;
            }
            throw new RuntimeException("Unexpected '!' at position " + pos + ". Did you mean '!='?");
        }
        return x;
    }

    private void nextChar() {
        ch = (++pos < expression.length()) ? expression.charAt(pos) : -1;
    }

    private boolean eat(int charToEat) {
        skipWhitespace();
        if (ch == charToEat) {
            nextChar();
            skipWhitespace(); // Skip whitespace after consuming character
            return true;
        }
        return false;
    }

    private Object parseExpression() {
        Object x = parseTerm();
        skipWhitespace();
        for (;;) {
            if (ch == '+') {
                nextChar();
                skipWhitespace();
                Object termObj = parseTerm();
                double xValue = objectToDouble(x);
                double termValue = objectToDouble(termObj);
                x = xValue + termValue; // addition
                skipWhitespace();
            }
            else if (ch == '-') {
                nextChar();
                skipWhitespace();
                Object termObj = parseTerm();
                double xValue = objectToDouble(x);
                double termValue = objectToDouble(termObj);
                x = xValue - termValue; // subtraction
                skipWhitespace();
            }
            else return x;
        }
    }

    private Object parseTerm() {
        Object x = parseFactor();
        skipWhitespace();
        for (;;) {
            if (ch == '*') {
                nextChar();
                skipWhitespace();
                Object factorObj = parseFactor();
                double xValue = objectToDouble(x);
                double factorValue = objectToDouble(factorObj);
                x = xValue * factorValue; // multiplication
                skipWhitespace();
            }
            else if (ch == '/') {
                nextChar();
                skipWhitespace();
                Object factorObj = parseFactor();
                double xValue = objectToDouble(x);
                double factorValue = objectToDouble(factorObj);
                if (Math.abs(factorValue) < 0.0001) {
                    throw new RuntimeException("Division by zero");
                }
                x = xValue / factorValue; // division
                skipWhitespace();
            }
            else if (ch == '#') {
                nextChar();
                skipWhitespace();
                Object factorObj = parseFactor();
                double xValue = objectToDouble(x);
                double factorValue = objectToDouble(factorObj);
                if (Math.abs(factorValue) < 0.0001) {
                    throw new RuntimeException("Division by zero");
                }
                x = Math.floor(xValue / factorValue); // floor division
                skipWhitespace();
            }
            else if (ch == '%') {
                nextChar();
                skipWhitespace();
                Object factorObj = parseFactor();
                double xValue = objectToDouble(x);
                double factorValue = objectToDouble(factorObj);
                if (Math.abs(factorValue) < 0.0001) {
                    throw new RuntimeException("Division by zero");
                }
                x = xValue % factorValue; // modulus
                skipWhitespace();
            }
            else return x;
        }
    }

    private double objectToDouble(Object obj) {
        if (obj instanceof Number) {
            return ((Number) obj).doubleValue();
        }
        else if (obj instanceof Boolean) {
            return ((Boolean) obj) ? 1.0 : 0.0;
        }
        else {
            throw new RuntimeException("Cannot convert " + obj + " to a number");
        }
    }

    private Object parseFactor() {
        skipWhitespace();
        
        // Handle pre-increment and pre-decrement
        if (ch == '+') {
            int nextPos = pos + 1;
            if (nextPos < expression.length() && expression.charAt(nextPos) == '+') {
                nextChar(); // consume first +
                nextChar(); // consume second +
                skipWhitespace();
                
                // Parse variable name
                StringBuilder varName = new StringBuilder();
                while ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || 
                       (ch >= '0' && ch <= '9') || ch == '_') {
                    varName.append((char)ch);
                    nextChar();
                }
                
                String variable = varName.toString();
                Object currentValue = environment.getVariable(variable);
                
                if (currentValue == null) {
                    throw new RuntimeException("Undefined variable: " + variable);
                }
                
                if (!(currentValue instanceof Number)) {
                    throw new RuntimeException("Cannot increment non-numeric variable: " + variable);
                }
                
                double currentNum = ((Number) currentValue).doubleValue();
                double newValue = currentNum + 1;
                
                // Store and return the new value
                if (currentValue instanceof Integer) {
                    environment.setVariable(variable, (int) newValue);
                    return (int) newValue;
                } else {
                    environment.setVariable(variable, newValue);
                    return newValue;
                }
            } else {
                nextChar(); // consume +
                skipWhitespace();
                return parseFactor(); // unary plus
            }
        }
        
        if (ch == '-') {
            int nextPos = pos + 1;
            if (nextPos < expression.length() && expression.charAt(nextPos) == '-') {
                nextChar(); // consume first -
                nextChar(); // consume second -
                skipWhitespace();
                
                // Parse variable name
                StringBuilder varName = new StringBuilder();
                while ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || 
                       (ch >= '0' && ch <= '9') || ch == '_') {
                    varName.append((char)ch);
                    nextChar();
                }
                
                String variable = varName.toString();
                Object currentValue = environment.getVariable(variable);
                
                if (currentValue == null) {
                    throw new RuntimeException("Undefined variable: " + variable);
                }
                
                if (!(currentValue instanceof Number)) {
                    throw new RuntimeException("Cannot decrement non-numeric variable: " + variable);
                }
                
                double currentNum = ((Number) currentValue).doubleValue();
                double newValue = currentNum - 1;
                
                // Store and return the new value
                if (currentValue instanceof Integer) {
                    environment.setVariable(variable, (int) newValue);
                    return (int) newValue;
                } else {
                    environment.setVariable(variable, newValue);
                    return newValue;
                }
            } else {
                nextChar(); // consume -
                skipWhitespace();
                Object factor = parseFactor();
                return -objectToDouble(factor); // unary minus
            }
        }

        Object x;
        int startPos = this.pos;
        if (ch == '(') { // parentheses
            nextChar(); // consume (
            skipWhitespace();
            x = parseAssignment(); // Allow walrus operator inside parentheses
            skipWhitespace();
            if (ch != ')') {
                throw new RuntimeException("Missing closing parenthesis at position " + pos);
            }
            nextChar(); // consume )
            skipWhitespace();
        }
        
        else if ((ch >= '0' && ch <= '9') || ch == '.') { // numbers
            while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
            x = Double.parseDouble(expression.substring(startPos, this.pos));
            skipWhitespace();
        }
        
        else if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || ch == '_') { // functions or variables
            StringBuilder identifier = new StringBuilder();
            while ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9') || ch == '_') {
                identifier.append((char)ch);
                nextChar();
            }
            skipWhitespace();
            
            // Check for post-increment/decrement
            if (ch == '+') {
                int nextPos = pos + 1;
                if (nextPos < expression.length() && expression.charAt(nextPos) == '+') {
                    nextChar(); // consume first +
                    nextChar(); // consume second +
                    skipWhitespace();
                    
                    String variable = identifier.toString();
                    Object currentValue = environment.getVariable(variable);
                    
                    if (currentValue == null) {
                        throw new RuntimeException("Undefined variable: " + variable);
                    }
                    
                    if (!(currentValue instanceof Number)) {
                        throw new RuntimeException("Cannot increment non-numeric variable: " + variable);
                    }
                    
                    double currentNum = ((Number) currentValue).doubleValue();
                    double newValue = currentNum + 1;
                    
                    // Store the new value but return the original value
                    if (currentValue instanceof Integer) {
                        environment.setVariable(variable, (int) newValue);
                        return (int) currentNum;
                    } else {
                        environment.setVariable(variable, newValue);
                        return currentNum;
                    }
                }
            } else if (ch == '-') {
                int nextPos = pos + 1;
                if (nextPos < expression.length() && expression.charAt(nextPos) == '-') {
                    nextChar(); // consume first -
                    nextChar(); // consume second -
                    skipWhitespace();
                    
                    String variable = identifier.toString();
                    Object currentValue = environment.getVariable(variable);
                    
                    if (currentValue == null) {
                        throw new RuntimeException("Undefined variable: " + variable);
                    }
                    
                    if (!(currentValue instanceof Number)) {
                        throw new RuntimeException("Cannot decrement non-numeric variable: " + variable);
                    }
                    
                    double currentNum = ((Number) currentValue).doubleValue();
                    double newValue = currentNum - 1;
                    
                    // Store the new value but return the original value
                    if (currentValue instanceof Integer) {
                        environment.setVariable(variable, (int) newValue);
                        return (int) currentNum;
                    } else {
                        environment.setVariable(variable, newValue);
                        return currentNum;
                    }
                }
            }
            
            // Look ahead for module operator ::
            if (ch == ':') {
                nextChar(); // consume first :
                if (ch == ':') {
                    nextChar(); // consume second :
                    skipWhitespace();
                    
                    // Parse function name after ::
                    StringBuilder functionName = new StringBuilder();
                    while ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || 
                           (ch >= '0' && ch <= '9') || ch == '_') {
                        functionName.append((char)ch);
                        nextChar();
                    }
                    skipWhitespace();
                    String fullName = identifier + "::" + functionName;
                    
                    // Handle module function call with proper argument parsing
                    if (ch == '(') {
                        nextChar(); // consume (
                        skipWhitespace();
                        List<Object> args = new ArrayList<>();
                        if (ch != ')') {  // If not empty arguments
                            while (true) {
                                args.add(parseAssignment()); // Support walrus operator in arguments
                                skipWhitespace();
                                if (ch == ')') {
                                    nextChar(); // consume )
                                    break;
                                }
                                if (ch != ',') {
                                    throw new RuntimeException("Expected ',' or ')' in argument list at position " + pos);
                                }
                                nextChar(); // consume ,
                                skipWhitespace();
                            }
                        } else {
                            nextChar(); // consume )
                        }
                        skipWhitespace();
                        
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
                    throw new RuntimeException("Expected '(' after module function " + fullName);
                } else {
                    pos--; // Go back if second : is not found
                    ch = expression.charAt(pos);
                }
            }
            
            // Handle regular functions or variables
            String func = identifier.toString();
            if (ch == '(') {
                nextChar(); // consume (
                skipWhitespace();
                List<Object> args = new ArrayList<>();
                if (ch != ')') {  // If not empty arguments
                    while (true) {
                        args.add(parseAssignment()); // Support walrus operator in arguments
                        skipWhitespace();
                        if (ch == ')') {
                            nextChar(); // consume )
                            break;
                        }
                        if (ch != ',') {
                            throw new RuntimeException("Expected ',' or ')' in argument list at position " + pos);
                        }
                        nextChar(); // consume ,
                        skipWhitespace();
                    }
                } else {
                    nextChar(); // consume )
                }
                skipWhitespace();
                
                // Handle function call
                Function function = environment.getFunction(func);
                if (function != null) {
                    // Convert arguments to strings for Executor.executeFunction
                    String[] argStrings = new String[args.size()];
                    for (int i = 0; i < args.size(); i++) {
                        // Convert the evaluated expression to a string that can be re-evaluated
                        Object arg = args.get(i);
                        if (arg instanceof Double) {
                            // Format doubles nicely (without unnecessary decimal places)
                            double d = (Double) arg;
                            if (d == Math.floor(d)) {
                                argStrings[i] = String.format("%.0f", d);
                            } else {
                                argStrings[i] = arg.toString();
                            }
                        } else {
                            argStrings[i] = arg.toString();
                        }
                    }
                    
                    Executor executor = new Executor(environment);
                    return executor.executeFunction(func, argStrings);
                } else {
                    throw new RuntimeException("Function not found: " + func);
                }
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
        else if (ch == -1) {
            throw new RuntimeException("Unexpected end of expression");
        }
        else {
            throw new RuntimeException("Unexpected character: '" + (char)ch + "' at position " + pos);
        }

        // Handle exponentiation
        skipWhitespace();
        if (ch == '^') {
            nextChar(); // consume ^
            skipWhitespace();
            Object factorObj = parseFactor();
            double xValue = objectToDouble(x);
            double factorValue = objectToDouble(factorObj);
            x = Math.pow(xValue, factorValue); // exponentiation
            skipWhitespace();
        }
        return x;
    }

    // Helper method to skip whitespace
    private void skipWhitespace() {
        while (pos < expression.length() && 
               (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n')) {
            nextChar();
        }
    }
}
