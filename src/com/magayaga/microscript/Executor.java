/**
 * MicroScript — The programming language
 * Copyright (c) 2024-2025 Cyril John Magayaga
 * 
 * It was originally written in Java programming language.
 */
package com.magayaga.microscript;

import java.util.List;
import java.util.ArrayList;
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

            // Handle if statements directly
            if (expression.startsWith("if")) {
                // If statements should be handled by Statements class
                // but this is for direct execution from command line or REPL
                Statements.processConditionalStatement(
                    Arrays.asList(expression), 0, this);
                return;
            }

            if (expression.startsWith("console.write")) {
                // Extract the content inside console.write()
                Pattern pattern = Pattern.compile("console\\.write\\((.*)\\);");
                Matcher matcher = pattern.matcher(expression);
                if (matcher.matches()) {
                    String innerContent = matcher.group(1).trim();
                    
                    // Split the content by commas, but respect quotes and parentheses
                    List<String> arguments = splitArguments(innerContent);
                    
                    if (arguments.isEmpty()) {
                        throw new RuntimeException("console.write() requires at least one argument");
                    }
                    
                    // Process first argument
                    Object firstArg = evaluate(arguments.get(0));
                    
                    // Handle case where first argument is not a string template
                    if (!(firstArg instanceof String)) {
                        // For non-string values, just print them directly
                        System.out.println(firstArg);
                        return;
                    }
                    
                    // Process as string template if it's a string
                    String template = (String) firstArg;
                    
                    // Process the template with expressions - {expression} style
                    StringBuffer output = new StringBuffer();
                    Pattern exprPattern = Pattern.compile("\\{([^{}]+)\\}");
                    Matcher placeholderMatcher = exprPattern.matcher(template);
                    
                    while (placeholderMatcher.find()) {
                        String expr = placeholderMatcher.group(1).trim();
                        
                        try {
                            // Evaluate the expression inside the braces
                            Object result = evaluate(expr);
                            // Replace with the string representation of the result
                            placeholderMatcher.appendReplacement(output, result.toString().replace("$", "\\$"));
                        }
                        
                        catch (Exception e) {
                            // If evaluation fails, leave the placeholder as is
                            placeholderMatcher.appendReplacement(output, "{" + expr + "}");
                        }
                    }
                    placeholderMatcher.appendTail(output);
                    
                    // Process positional placeholders - {} style
                    String result = output.toString();
                    if (arguments.size() > 1) {
                        // If there are additional arguments, handle positional placeholders
                        StringBuffer positionalOutput = new StringBuffer();
                        Pattern positionalPattern = Pattern.compile("\\{\\}");
                        Matcher positionalMatcher = positionalPattern.matcher(result);
                        
                        int argIndex = 1; // Start from the second argument
                        while (positionalMatcher.find() && argIndex < arguments.size()) {
                            Object argValue = evaluate(arguments.get(argIndex++));
                            positionalMatcher.appendReplacement(positionalOutput, 
                                argValue == null ? "null" : argValue.toString().replace("$", "\\$"));
                        }
                        positionalMatcher.appendTail(positionalOutput);
                        result = positionalOutput.toString();
                    }
                    
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
                        case "Int64":
                            if (!(value instanceof Integer)) {
                                throw new RuntimeException("Type error: " + valueExpression + " is not an Integer.");
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
                        case "Char":
                            if (!(value instanceof Character)) {
                                throw new RuntimeException("Type error: " + valueExpression + " is not a Character.");
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
            
            else if (expression.startsWith("return")) {
                // Handle return statements
                // This will be processed in executeFunction
                return;
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

    // Helper method to split arguments respecting quotes and nested structures
    private List<String> splitArguments(String content) {
        List<String> result = new ArrayList<>();
        int start = 0;
        int level = 0;
        boolean inQuotes = false;
        
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            
            if (c == '"' && (i == 0 || content.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
            }
            
            else if (!inQuotes) {
                if (c == '(') {
                    level++;
                }
                
                else if (c == ')') {
                    level--;
                }
                
                else if (c == ',' && level == 0) {
                    result.add(content.substring(start, i).trim());
                    start = i + 1;
                }
            }
        }
        
        // Add the last argument
        if (start < content.length()) {
            result.add(content.substring(start).trim());
        }
        
        return result;
    }

    private void executeSystemCommand(String command) throws Exception {
        String[] cmdArray = command.split(" ");
        Process process = new ProcessBuilder(cmdArray).start();
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
        reader.close();
    }

    public Object executeFunction(String functionName, String[] args) {
        Function function = environment.getFunction(functionName);
        if (function != null) {
            List<Parameter> parameters = function.getParameters();
            if (args == null) {
                args = new String[0]; // Ensure args is not null
            }
            if (parameters.size() != args.length) {
                throw new RuntimeException("Argument count mismatch for function: " + functionName);
            }

            Environment localEnv = new Environment(environment);
            for (int i = 0; i < args.length; i++) {
                Object value = evaluate(args[i]);
                String expectedType = parameters.get(i).getType();
                // Ensure the value matches the expected type
                switch (expectedType) {
                    case "String":
                        if (!(value instanceof String)) {
                            throw new RuntimeException("Type error: Argument " + args[i] + " is not a String.");
                        }
                        break;
                    case "Int32":
                    case "Int64":
                        if (!(value instanceof Integer)) {
                            throw new RuntimeException("Type error: Argument " + args[i] + " is not an Integer.");
                        }
                        break;
                    case "Float32":
                        if (!(value instanceof Float)) {
                            throw new RuntimeException("Type error: Argument " + args[i] + " is not a Float32.");
                        }
                        break;
                    case "Float64":
                        if (!(value instanceof Double)) {
                            throw new RuntimeException("Type error: Argument " + args[i] + " is not a Float64.");
                        }
                        break;
                    case "Char":
                        if (!(value instanceof Character)) {
                            throw new RuntimeException("Type error: Argument " + args[i] + " is not a Character.");
                        }
                        break;
                    default:
                        throw new RuntimeException("Unknown type annotation: " + expectedType);
                }
                localEnv.setVariable(parameters.get(i).getName(), value);
            }

            Object returnValue = null;
            List<String> body = function.getBody();
            // Process function body, handling control flow structures like if/else
            for (int i = 0; i < body.size(); i++) {
                String line = body.get(i).trim();
                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("//")) {
                    continue;
                }
                
                // Handle if statements
                if (line.startsWith("if")) {
                    // Create an executor with the local environment to ensure variables are accessible
                    Executor localExecutor = new Executor(localEnv);
                    // Use the Statements class to process the conditional
                    int newIndex = Statements.processConditionalStatement(body, i, localExecutor);
                    
                    // Important: Make sure we're not stuck in an infinite loop
                    if (newIndex <= i) {
                        throw new RuntimeException("Error processing if statement at line: " + line);
                    }
                    
                    i = newIndex - 1; // -1 because the loop will increment i
                    continue;
                }
                
                // Handle while loops
                if (line.startsWith("while")) {
                    // Create an executor with the local environment
                    Executor localExecutor = new Executor(localEnv);
                    // Process the while loop
                    int newIndex = Loop.processLoop(body, i, localExecutor);
                    
                    // Ensure we're making progress
                    if (newIndex <= i) {
                        throw new RuntimeException("Error processing while loop at line: " + line);
                    }
                    
                    i = newIndex - 1; // -1 because the loop will increment i
                    continue;
                }
                
                // Handle return statements
                if (line.startsWith("return")) {
                    String returnExpression = line.substring(line.indexOf("return") + 6).trim().replace(";", "");
                    // Evaluate complex expressions in return statements
                    returnValue = new Executor(localEnv).evaluate(returnExpression);
                    // Ensure the return value matches the expected return type
                    String expectedReturnType = function.getReturnType();
                    switch (expectedReturnType) {
                        case "String":
                            if (!(returnValue instanceof String)) {
                                throw new RuntimeException("Type error: Return value " + returnValue + " is not a String.");
                            }
                            break;
                        case "Int32":
                        case "Int64":
                            if (!(returnValue instanceof Integer)) {
                                throw new RuntimeException("Type error: Return value " + returnValue + " is not an Integer.");
                            }
                            break;
                        case "Float32":
                            if (!(returnValue instanceof Float)) {
                                throw new RuntimeException("Type error: Return value " + returnValue + " is not a Float32.");
                            }
                            break;
                        case "Float64":
                            if (!(returnValue instanceof Double)) {
                                // Convert Integer to Double if necessary
                                if (returnValue instanceof Integer) {
                                    returnValue = ((Integer) returnValue).doubleValue();
                                } else {
                                    throw new RuntimeException("Type error: Return value " + returnValue + " is not a Float64.");
                                }
                            }
                            break;
                        case "Char":
                            if (!(returnValue instanceof Character)) {
                                throw new RuntimeException("Type error: Return value " + returnValue + " is not a Character.");
                            }
                            break;
                        default:
                            throw new RuntimeException("Unknown return type annotation: " + expectedReturnType);
                    }
                    return returnValue; // Exit the function immediately after return
                }
                // Use a local executor to ensure variable modifications are retained
                new Executor(localEnv).execute(line);
            }
            return returnValue;
        }
        // Support for native functions (Import.FunctionInterface)
        Object nativeFunc = environment.getVariable(functionName);
        if (nativeFunc instanceof Import.FunctionInterface) {
            Object[] evaluatedArgs = new Object[args == null ? 0 : args.length];
            for (int i = 0; i < evaluatedArgs.length; i++) {
                evaluatedArgs[i] = evaluate(args[i]);
            }
            return ((Import.FunctionInterface) nativeFunc).call(evaluatedArgs);
        }
        throw new RuntimeException("Function not found: " + functionName);
    }

    public Object evaluate(String expression) {
        // Skip empty expressions
        if (expression == null || expression.trim().isEmpty()) {
            return null;
        }

        // Check if the expression is a string literal
        if (expression.startsWith("\"") && expression.endsWith("\"")) {
            return expression.substring(1, expression.length() - 1);
        }

        // Check if the expression is a character literal
        if (expression.startsWith("'") && expression.endsWith("'") && expression.length() == 3) {
            return expression.charAt(1);
        }

        // Check if the expression is a function call
        Pattern functionCallPattern = Pattern.compile("(\\w+)\\((.*)\\)");
        Matcher matcher = functionCallPattern.matcher(expression);
        if (matcher.matches()) {
            String functionName = matcher.group(1);
            String args = matcher.group(2).trim();
            String[] arguments = args.isEmpty() ? new String[0] : splitArguments(args).toArray(new String[0]);
            return executeFunction(functionName, arguments);
        }

        // Example for function call:
        if (expression.startsWith("math::sqrt(")) {
            String argStr = expression.substring("math::sqrt(".length(), expression.length() - 1);
            double arg = Double.parseDouble(argStr);
            Import.FunctionInterface sqrtFunc = (Import.FunctionInterface) environment.getVariable("math::sqrt");
            return sqrtFunc.call(new Object[]{arg});
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

        // Handle ternary expressions in the main evaluate method
        int questionPos = expression.indexOf('?');
        if (questionPos > 0) {
            int colonPos = expression.indexOf(':', questionPos);
            if (colonPos > questionPos) {
                String condition = expression.substring(0, questionPos).trim();
                String trueExpr = expression.substring(questionPos + 1, colonPos).trim();
                String falseExpr = expression.substring(colonPos + 1).trim();
                
                Object condValue = evaluate(condition);
                boolean condResult = false;
                
                if (condValue instanceof Number) {
                    condResult = ((Number) condValue).doubleValue() != 0;
                } else if (condValue instanceof Boolean) {
                    condResult = (Boolean) condValue;
                }
                
                return condResult ? evaluate(trueExpr) : evaluate(falseExpr);
            }
        }

        // Otherwise, treat it as an arithmetic expression
        ExpressionEvaluator evaluator = new ExpressionEvaluator(expression, environment);
        return evaluator.parse();
    }
}
