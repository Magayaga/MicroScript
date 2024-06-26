/**
 * MicroScript — The programming language
 * Copyright (c) 2024 Cyril John Magayaga
 * 
 * It was originally written in Java programming language.
 */
package com.magayaga.microscript;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Executor {
    private final Environment environment;

    public Executor(Environment environment) {
        this.environment = environment;
    }

    public void execute(String expression) {
        try {
            if (expression.startsWith("console.write")) {
                // Extract the content inside console.write()
                Pattern pattern = Pattern.compile("console.write\\((.*)\\);");
                Matcher matcher = pattern.matcher(expression);
                if (matcher.matches()) {
                    String innerExpression = matcher.group(1).trim();
                    Object result = evaluate(innerExpression);
                    System.out.println(result);
                }
            } else {
                // Evaluate as a general expression (for variable assignments, etc.)
                evaluate(expression);
            }
        } catch (Exception e) {
            System.out.println("Evaluation error: " + e.getMessage());
        }
    }

    public Object executeFunction(String functionName, String[] args) {
        Function function = environment.getFunction(functionName);
        if (function == null) {
            throw new RuntimeException("Function not found: " + functionName);
        }

        List<String> parameters = function.getParameters();
        if (args == null) {
            args = new String[0]; // Ensure args is not null
        }
        if (parameters.size() != args.length) {
            throw new RuntimeException("Argument count mismatch for function: " + functionName);
        }

        Environment localEnv = new Environment(environment);
        for (int i = 0; i < args.length; i++) {
            Object value = evaluate(args[i]);
            localEnv.setVariable(parameters.get(i), value);
        }

        Object returnValue = null; // Default return value if no return statement
        for (String line : function.getBody()) {
            if (line.trim().startsWith("return")) {
                String returnExpression = line.substring(line.indexOf("return") + 6, line.indexOf(";")).trim();
                returnValue = evaluate(returnExpression);
                break;
            }
            new Executor(localEnv).execute(line);
        }

        return returnValue;
    }

    public Object evaluate(String expression) {
        // Check if the expression is a string literal
        if (expression.startsWith("\"") && expression.endsWith("\"")) {
            return expression.substring(1, expression.length() - 1);
        }

        // Check if the expression is a function call
        Pattern functionCallPattern = Pattern.compile("(\\w+)\\((.*)\\)");
        Matcher matcher = functionCallPattern.matcher(expression);
        if (matcher.matches()) {
            String functionName = matcher.group(1);
            String args = matcher.group(2).trim();
            String[] arguments = args.isEmpty() ? new String[0] : args.split("\\s*,\\s*");
            return executeFunction(functionName, arguments);
        }

        // Check if the expression is a variable
        Object variableValue = environment.getVariable(expression);
        if (variableValue != null) {
            return variableValue;
        }

        // Otherwise, treat it as an arithmetic expression
        ExpressionEvaluator evaluator = new ExpressionEvaluator(expression, environment);
        return evaluator.parse();
    }
}
