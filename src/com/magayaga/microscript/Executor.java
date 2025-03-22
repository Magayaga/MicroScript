package com.magayaga.microscript;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Arrays;

public class Executor {
    private final Environment environment;

    public Executor(Environment environment) {
        this.environment = environment;
    }

    public void execute(String expression) {
        try {
            // Skip comments
            if (expression.startsWith("//")) {
                return;
            }

            if (expression.startsWith("console.write")) {
                // Extract the content inside console.write()
                Pattern pattern = Pattern.compile("console.write\\((.*)\\);");
                Matcher matcher = pattern.matcher(expression);
                if (matcher.matches()) {
                    String innerExpression = matcher.group(1).trim();
                    Object result = evaluate(innerExpression);
                    System.out.println(result);
                }
            }

            else if (expression.startsWith("console.system")) {
                // Extract the command inside console.system()
                Pattern pattern = Pattern.compile("console.system\\((.*)\\);");
                Matcher matcher = pattern.matcher(expression);
                if (matcher.matches()) {
                    String command = matcher.group(1).trim();
                    executeSystemCommand(command);
                }
            }
            
            else if (expression.startsWith("var ")) {
                // Handle variable declaration with type annotation
                String declaration = expression.substring(4).trim();
                int equalsIndex = declaration.indexOf('=');
                if (equalsIndex != -1) {
                    String varDeclaration = declaration.substring(0, equalsIndex).trim();
                    String[] parts = varDeclaration.split(":");
                    if (parts.length != 2) {
                        throw new RuntimeException("Syntax error in variable declaration: " + expression);
                    }
                    String varName = parts[0].trim();
                    String typeAnnotation = parts[1].trim();
                    String valueExpression = declaration.substring(equalsIndex + 1).trim().replace(";", "");
                    Object value = evaluate(valueExpression);

                    // Ensure the value matches the type annotation
                    switch (typeAnnotation) {
                        case "String":
                            if (!(value instanceof String)) {
                                throw new RuntimeException("Type error: " + valueExpression + " is not a String.");
                            }
                            break;
                        case "Int32":
                            if (!(value instanceof Integer)) {
                                throw new RuntimeException("Type error: " + valueExpression + " is not an Int32.");
                            }
                            break;
                        case "Int64":
                            if (!(value instanceof Long)) {
                                throw new RuntimeException("Type error: " + valueExpression + " is not an Int64.");
                            }
                            break;
                        case "Float32":
                            if (!(value instanceof Float)) {
                                throw new RuntimeException("Type error: " + valueExpression + " is not a Float32.");
                            }
                            break;
                        case "Float64":
                            if (!(value instanceof Double)) {
                                throw new RuntimeException("Type error: " + valueExpression + " is not a Float64.");
                            }
                            break;
                        default:
                            throw new RuntimeException("Unknown type annotation: " + typeAnnotation);
                    }

                    environment.setVariable(varName, value);
                }
                
                else {
                    throw new RuntimeException("Syntax error in variable declaration: " + expression);
                }
            }
            
            else if (expression.startsWith("bool ")) {
                // Handle boolean declaration
                String declaration = expression.substring(5).trim();
                int equalsIndex = declaration.indexOf('=');
                if (equalsIndex != -1) {
                    String boolName = declaration.substring(0, equalsIndex).trim();
                    String valueExpression = declaration.substring(equalsIndex + 1).trim().replace(";", "");
                    Object value = evaluate(valueExpression);
                    if (value instanceof Boolean) {
                        environment.setVariable(boolName, value);
                    }
                    
                    else {
                        throw new RuntimeException("Syntax error: " + valueExpression + " is not a boolean.");
                    }
                }
                
                else {
                    throw new RuntimeException("Syntax error in boolean declaration: " + expression);
                }
            }

            else if (expression.startsWith("list ")) {
                // Handle list declaration
                String declaration = expression.substring(5).trim();
                int equalsIndex = declaration.indexOf('=');
                if (equalsIndex != -1) {
                    String listName = declaration.substring(0, equalsIndex).trim();
                    String valueExpression = declaration.substring(equalsIndex + 1).trim().replace(";", "");
                    if (valueExpression.startsWith("[") && valueExpression.endsWith("]")) {
                        String elements = valueExpression.substring(1, valueExpression.length() - 1);
                        ListVariable list = new ListVariable(elements.split("\\s*,\\s*"));
                        environment.setVariable(listName, list);
                    }
                    
                    else {
                        throw new RuntimeException("Syntax error in list declaration: " + valueExpression);
                    }
                }
                
                else {
                    throw new RuntimeException("Syntax error in list declaration: " + expression);
                }
            }
            
            else {
                // Evaluate as a general expression (for variable assignments, etc.)
                evaluate(expression);
            }
        }
        
        catch (Exception e) {
            System.out.println("Evaluation error: " + e.getMessage());
        }
    }

    private void executeSystemCommand(String command) throws Exception {
        Process process = Runtime.getRuntime().exec(command);
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        reader.close();
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

        Object returnValue = null;
        for (String line : function.getBody()) {
            if (line.trim().startsWith("return")) {
                String returnExpression = line.substring(line.indexOf("return") + 6).trim().replace(";", "");
                returnValue = new Executor(localEnv).evaluate(returnExpression);
                return returnValue; // Exit the function immediately after return
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

        // Check if the expression is a boolean literal or negation
        if (expression.equals("true")) {
            return true;
        }
        
        else if (expression.equals("false")) {
            return false;
        }
        
        else if (expression.startsWith("not ")) {
            Object value = evaluate(expression.substring(4).trim());
            if (value instanceof Boolean) {
                return !(Boolean) value;
            }
            
            else {
                throw new RuntimeException("Syntax error: " + expression + " is not a boolean.");
            }
        }
        
        else if (expression.startsWith("!")) {
            Object value = evaluate(expression.substring(1).trim());
            if (value instanceof Boolean) {
                return !(Boolean) value;
            }
            
            else {
                throw new RuntimeException("Syntax error: " + expression + " is not a boolean.");
            }
        }

        // Otherwise, treat it as an arithmetic expression
        ExpressionEvaluator evaluator = new ExpressionEvaluator(expression, environment);
        return evaluator.parse();
    }
}
