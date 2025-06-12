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
import java.io.IOException;

public class Executor {
    /* Change from private to package-private (default) access */
    final Environment environment;

    // Pre-compiled regex patterns
    private static final Pattern CONSOLE_WRITE_PATTERN = Pattern.compile("console\\.write\\((.*)\\);");
    private static final Pattern CONSOLE_WRITEF_PATTERN = Pattern.compile("console\\.writef\\((.*)\\);");
    private static final Pattern CONSOLE_PANIC_PATTERN = Pattern.compile("console\\.panic\\((.*)\\);");
    private static final Pattern CONSOLE_SYSTEM_PATTERN = Pattern.compile("console\\.system\\((.*)\\);");
    private static final Pattern FUNCTION_CALL_PATTERN = Pattern.compile("(\\w+)\\((.*)\\)");
    private static final Pattern STRING_TEMPLATE_EXPR_PATTERN = Pattern.compile("\\{([^{}]+)\\}");
    private static final Pattern STRING_TEMPLATE_POSITIONAL_PATTERN = Pattern.compile("\\{\\}");
    private static final Pattern SWITCH_DETECT_PATTERN = Pattern.compile("^\\s*switch\\s*\\(.*\\)\\s*\\{?\\s*$");
    private static final Pattern DEFINE_FUNC_MACRO_PATTERN =
        Pattern.compile("#define\\s+([A-Z_][A-Z0-9_]*)\\s*\\(([^)]*)\\)\\s+(.+)");
    
    // Patterns for increment/decrement operations
    private static final Pattern PRE_INCREMENT_PATTERN = Pattern.compile("\\+\\+([a-zA-Z_][a-zA-Z0-9_]*)\\s*;?");
    private static final Pattern PRE_DECREMENT_PATTERN = Pattern.compile("--([a-zA-Z_][a-zA-Z0-9_]*)\\s*;?");
    private static final Pattern POST_INCREMENT_PATTERN = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)\\+\\+\\s*;?");
    private static final Pattern POST_DECREMENT_PATTERN = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)--\\s*;?");

    public Executor(Environment environment) {
        this.environment = environment;
    }

    public static List<String> splitByCommaWithTrim(String input) {
        List<String> result = new ArrayList<>();
        if (input == null || input.isEmpty()) {
            return result;
        }
        String[] parts = input.split(",");
        for (String part : parts) {
            result.add(part.trim());
        }
        return result;
    }

    /**
     * Process escape sequences in a string
     * @param input The input string that may contain escape sequences
     * @return The processed string with escape sequences converted
     */
    private String processEscapeSequences(String input) {
        if (input == null) {
            return null;
        }
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            
            if (c == '\\' && i + 1 < input.length()) {
                char next = input.charAt(i + 1);
                switch (next) {
                    case 'n':
                        result.append('\n');
                        i++; // Skip the next character
                        break;
                    case 't':
                        result.append('\t');
                        i++; // Skip the next character
                        break;
                    case 'r':
                        result.append('\r');
                        i++; // Skip the next character
                        break;
                    case '\\':
                        result.append('\\');
                        i++; // Skip the next character
                        break;
                    case '"':
                        result.append('"');
                        i++; // Skip the next character
                        break;
                    case '\'':
                        result.append('\'');
                        i++; // Skip the next character
                        break;
                    case '0':
                        result.append('\0');
                        i++; // Skip the next character
                        break;
                    default:
                        // If it's not a recognized escape sequence, keep the backslash
                        result.append(c);
                        break;
                }
            } else {
                result.append(c);
            }
        }
        
        return result.toString();
    }

    public void execute(String expression) {
        try {
            // Skip comments
            if (expression.startsWith("//")) {
                return;
            }

            // Handle increment/decrement operations first
            if (handleIncrementDecrement(expression)) {
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

            // Handle for loop statements
            if (expression.startsWith("for")) {
                // For loops should be handled by ForLoop class
                // but this is for direct execution from command line or REPL
                ForLoop.processForLoop(Arrays.asList(expression), 0, this);
                return;
            }

            // Handle while loop statements
            if (expression.startsWith("while")) {
                // While loops should be handled by WhileLoop class
                // but this is for direct execution from command line or REPL
                WhileLoop.processWhileLoop(Arrays.asList(expression), 0, this);
                return;
            }

            if (expression.startsWith("console.write")) {
                // Check if it's console.writef first (more specific match)
                if (expression.startsWith("console.writef")) {
                    // Extract the content inside console.writef()
                    Matcher matcher = CONSOLE_WRITEF_PATTERN.matcher(expression);
                    if (matcher.matches()) {
                        String innerContent = matcher.group(1).trim();
                        
                        // Split the content by commas, but respect quotes and parentheses
                        List<String> arguments = splitArguments(innerContent);
                        
                        if (arguments.isEmpty()) {
                            throw new RuntimeException("console.writef() requires at least one argument");
                        }
                        
                        // Process first argument
                        Object firstArg = evaluate(arguments.get(0));
                        
                        // Handle case where first argument is not a string template
                        if (!(firstArg instanceof String)) {
                            // For non-string values, just print them directly without newline
                            System.out.print(firstArg);
                            return;
                        }
                        
                        // Process as string template if it's a string
                        String template = (String) firstArg;
                        
                        // Process escape sequences first
                        template = processEscapeSequences(template);
                        
                        // Process the template with expressions - {expression} style
                        StringBuffer output = new StringBuffer();
                        Matcher placeholderMatcher = STRING_TEMPLATE_EXPR_PATTERN.matcher(template);
                        
                        while (placeholderMatcher.find()) {
                            String expr = placeholderMatcher.group(1).trim();
                            
                            try {
                                // Evaluate the expression inside the braces
                                Object result = evaluate(expr);
                                // Replace with the string representation of the result
                                placeholderMatcher.appendReplacement(output, 
                                    Matcher.quoteReplacement(result.toString()));
                            }
                            catch (Exception e) {
                                // If evaluation fails, leave the placeholder as is
                                placeholderMatcher.appendReplacement(output, 
                                    Matcher.quoteReplacement("{" + expr + "}"));
                            }
                        }
                        placeholderMatcher.appendTail(output);
                        
                        // Process positional placeholders - {} style
                        String result = output.toString();
                        if (arguments.size() > 1) {
                            // If there are additional arguments, handle positional placeholders
                            StringBuffer positionalOutput = new StringBuffer();
                            Matcher positionalMatcher = STRING_TEMPLATE_POSITIONAL_PATTERN.matcher(result);
                            
                            int argIndex = 1; // Start from the second argument
                            while (positionalMatcher.find() && argIndex < arguments.size()) {
                                Object argValue = evaluate(arguments.get(argIndex++));
                                positionalMatcher.appendReplacement(positionalOutput, 
                                    Matcher.quoteReplacement(argValue == null ? "null" : argValue.toString()));
                            }
                            positionalMatcher.appendTail(positionalOutput);
                            result = positionalOutput.toString();
                        }
                        
                        // Print without newline for console.writef - this makes it work like bash command
                        System.out.print(result);
                        System.out.flush(); // Ensure immediate output for bash-like behavior
                    }
                }
                else {
                    // Handle regular console.write (with newline)
                    // Extract the content inside console.write()
                    Matcher matcher = CONSOLE_WRITE_PATTERN.matcher(expression);
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
                        Matcher placeholderMatcher = STRING_TEMPLATE_EXPR_PATTERN.matcher(template);
                        
                        while (placeholderMatcher.find()) {
                            String expr = placeholderMatcher.group(1).trim();
                            
                            try {
                                // Evaluate the expression inside the braces
                                Object result = evaluate(expr);
                                // Replace with the string representation of the result
                                placeholderMatcher.appendReplacement(output, 
                                    Matcher.quoteReplacement(result.toString()));
                            }
                            catch (Exception e) {
                                // If evaluation fails, leave the placeholder as is
                                placeholderMatcher.appendReplacement(output, 
                                    Matcher.quoteReplacement("{" + expr + "}"));
                            }
                        }
                        placeholderMatcher.appendTail(output);
                        
                        // Process positional placeholders - {} style
                        String result = output.toString();
                        if (arguments.size() > 1) {
                            // If there are additional arguments, handle positional placeholders
                            StringBuffer positionalOutput = new StringBuffer();
                            Matcher positionalMatcher = STRING_TEMPLATE_POSITIONAL_PATTERN.matcher(result);
                            
                            int argIndex = 1; // Start from the second argument
                            while (positionalMatcher.find() && argIndex < arguments.size()) {
                                Object argValue = evaluate(arguments.get(argIndex++));
                                positionalMatcher.appendReplacement(positionalOutput, 
                                    Matcher.quoteReplacement(argValue == null ? "null" : argValue.toString()));
                            }
                            positionalMatcher.appendTail(positionalOutput);
                            result = positionalOutput.toString();
                        }
                        
                        System.out.println(result);
                    }
                }
            }

                        else if (expression.startsWith("console.panic")) {
                // Handle console.panic() - signals a severe runtime error
                Matcher matcher = CONSOLE_PANIC_PATTERN.matcher(expression);
                if (matcher.matches()) {
                    String innerContent = matcher.group(1).trim();
                    
                    // Split the content by commas, but respect quotes and parentheses
                    List<String> arguments = splitArguments(innerContent);
                    
                    if (arguments.isEmpty()) {
                        throw new PanicException("panic: no message provided");
                    }
                    
                    // Process first argument (the panic message)
                    Object firstArg = evaluate(arguments.get(0));
                    
                    String panicMessage;
                    if (!(firstArg instanceof String)) {
                        panicMessage = firstArg == null ? "null" : firstArg.toString();
                    } else {
                        String template = (String) firstArg;
                        
                        // Process escape sequences first
                        template = processEscapeSequences(template);
                        
                        // Process the template with expressions - {expression} style
                        StringBuffer output = new StringBuffer();
                        Matcher placeholderMatcher = STRING_TEMPLATE_EXPR_PATTERN.matcher(template);
                        
                        while (placeholderMatcher.find()) {
                            String expr = placeholderMatcher.group(1).trim();
                            
                            try {
                                // Evaluate the expression inside the braces
                                Object result = evaluate(expr);
                                // Replace with the string representation of the result
                                placeholderMatcher.appendReplacement(output, 
                                    Matcher.quoteReplacement(result.toString()));
                            }
                            catch (Exception e) {
                                // If evaluation fails, leave the placeholder as is
                                placeholderMatcher.appendReplacement(output, 
                                    Matcher.quoteReplacement("{" + expr + "}"));
                            }
                        }
                        placeholderMatcher.appendTail(output);
                        
                        // Process positional placeholders - {} style
                        panicMessage = output.toString();
                        if (arguments.size() > 1) {
                            // If there are additional arguments, handle positional placeholders
                            StringBuffer positionalOutput = new StringBuffer();
                            Matcher positionalMatcher = STRING_TEMPLATE_POSITIONAL_PATTERN.matcher(panicMessage);
                            
                            int argIndex = 1; // Start from the second argument
                            while (positionalMatcher.find() && argIndex < arguments.size()) {
                                Object argValue = evaluate(arguments.get(argIndex++));
                                positionalMatcher.appendReplacement(positionalOutput, 
                                    Matcher.quoteReplacement(argValue == null ? "null" : argValue.toString()));
                            }
                            positionalMatcher.appendTail(positionalOutput);
                            panicMessage = positionalOutput.toString();
                        }
                    }
                    
                    // Print panic message to stderr and throw PanicException
                    System.err.println("panic: " + panicMessage);
                    System.err.flush();
                    
                    // Throw a special exception that will terminate the program
                    throw new PanicException("panic: " + panicMessage);
                }
            }

            else if (expression.startsWith("console.system")) {
                // Extract the command inside console.system()
                Matcher matcher = CONSOLE_SYSTEM_PATTERN.matcher(expression);
                if (matcher.matches()) {
                    String innerContent = matcher.group(1).trim();
                    
                    // Split the content by commas, but respect quotes and parentheses
                    List<String> arguments = splitArguments(innerContent);
                    
                    if (arguments.isEmpty()) {
                        throw new RuntimeException("console.system() requires at least one argument");
                    }
                    
                    // Process first argument (the command)
                    Object firstArg = evaluate(arguments.get(0));
                    
                    if (!(firstArg instanceof String)) {
                        throw new RuntimeException("console.system() command must be a string");
                    }
                    
                    String command = (String) firstArg;
                    
                    // Process escape sequences
                    command = processEscapeSequences(command);
                    
                    // Process the command with expressions - {expression} style
                    StringBuffer output = new StringBuffer();
                    Matcher placeholderMatcher = STRING_TEMPLATE_EXPR_PATTERN.matcher(command);
                    
                    while (placeholderMatcher.find()) {
                        String expr = placeholderMatcher.group(1).trim();
                        
                        try {
                            // Evaluate the expression inside the braces
                            Object result = evaluate(expr);
                            // Replace with the string representation of the result
                            placeholderMatcher.appendReplacement(output, 
                                Matcher.quoteReplacement(result.toString()));
                        }
                        catch (Exception e) {
                            // If evaluation fails, leave the placeholder as is
                            placeholderMatcher.appendReplacement(output, 
                                Matcher.quoteReplacement("{" + expr + "}"));
                        }
                    }
                    placeholderMatcher.appendTail(output);
                    
                    // Process positional placeholders - {} style
                    String result = output.toString();
                    if (arguments.size() > 1) {
                        // If there are additional arguments, handle positional placeholders
                        StringBuffer positionalOutput = new StringBuffer();
                        Matcher positionalMatcher = STRING_TEMPLATE_POSITIONAL_PATTERN.matcher(result);
                        
                        int argIndex = 1; // Start from the second argument
                        while (positionalMatcher.find() && argIndex < arguments.size()) {
                            Object argValue = evaluate(arguments.get(argIndex++));
                            positionalMatcher.appendReplacement(positionalOutput, 
                                Matcher.quoteReplacement(argValue == null ? "null" : argValue.toString()));
                        }
                        positionalMatcher.appendTail(positionalOutput);
                        result = positionalOutput.toString();
                    }
                    
                    // Execute the processed command
                    executeSystemCommand(result);
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
                        ListVariable list = new ListVariable(splitByCommaWithTrim(elements).toArray(new String[0]));
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

            else if (expression.trim().startsWith("switch")) {
                // Handle both inline and block form of switch statements
                String trimmedExpr = expression.trim();
                if (trimmedExpr.endsWith("{")) {
                    trimmedExpr = trimmedExpr.substring(0, trimmedExpr.length() - 1).trim();
                }
                Switch.processSwitchStatement(Arrays.asList(trimmedExpr, "{"), 0, this);
                return;
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

    /**
     * Handle increment and decrement operations (++var, var++, --var, var--)
     * @param expression The expression to check and potentially execute
     * @return true if the expression was an increment/decrement operation and was handled
     */
    private boolean handleIncrementDecrement(String expression) {
        String trimmed = expression.trim();
        
        // Handle pre-increment (++var)
        Matcher preIncMatcher = PRE_INCREMENT_PATTERN.matcher(trimmed);
        if (preIncMatcher.matches()) {
            String varName = preIncMatcher.group(1);
            Object currentValue = environment.getVariable(varName);
            
            if (currentValue == null) {
                throw new RuntimeException("Undefined variable: " + varName);
            }
            
            if (!(currentValue instanceof Number)) {
                throw new RuntimeException("Cannot increment non-numeric variable: " + varName);
            }
            
            double currentNum = ((Number) currentValue).doubleValue();
            double newValue = currentNum + 1;
            
            // Store the new value
            if (currentValue instanceof Integer) {
                environment.setVariable(varName, (int) newValue);
            } else {
                environment.setVariable(varName, newValue);
            }
            return true;
        }
        
        // Handle pre-decrement (--var)
        Matcher preDecMatcher = PRE_DECREMENT_PATTERN.matcher(trimmed);
        if (preDecMatcher.matches()) {
            String varName = preDecMatcher.group(1);
            Object currentValue = environment.getVariable(varName);
            
            if (currentValue == null) {
                throw new RuntimeException("Undefined variable: " + varName);
            }
            
            if (!(currentValue instanceof Number)) {
                throw new RuntimeException("Cannot decrement non-numeric variable: " + varName);
            }
            
            double currentNum = ((Number) currentValue).doubleValue();
            double newValue = currentNum - 1;
            
            // Store the new value
            if (currentValue instanceof Integer) {
                environment.setVariable(varName, (int) newValue);
            } else {
                environment.setVariable(varName, newValue);
            }
            return true;
        }
        
        // Handle post-increment (var++)
        Matcher postIncMatcher = POST_INCREMENT_PATTERN.matcher(trimmed);
        if (postIncMatcher.matches()) {
            String varName = postIncMatcher.group(1);
            Object currentValue = environment.getVariable(varName);
            
            if (currentValue == null) {
                throw new RuntimeException("Undefined variable: " + varName);
            }
            
            if (!(currentValue instanceof Number)) {
                throw new RuntimeException("Cannot increment non-numeric variable: " + varName);
            }
            
            double currentNum = ((Number) currentValue).doubleValue();
            double newValue = currentNum + 1;
            
            // Store the new value
            if (currentValue instanceof Integer) {
                environment.setVariable(varName, (int) newValue);
            } else {
                environment.setVariable(varName, newValue);
            }
            return true;
        }
        
        // Handle post-decrement (var--)
        Matcher postDecMatcher = POST_DECREMENT_PATTERN.matcher(trimmed);
        if (postDecMatcher.matches()) {
            String varName = postDecMatcher.group(1);
            Object currentValue = environment.getVariable(varName);
            
            if (currentValue == null) {
                throw new RuntimeException("Undefined variable: " + varName);
            }
            
            if (!(currentValue instanceof Number)) {
                throw new RuntimeException("Cannot decrement non-numeric variable: " + varName);
            }
            
            double currentNum = ((Number) currentValue).doubleValue();
            double newValue = currentNum - 1;
            
            // Store the new value
            if (currentValue instanceof Integer) {
                environment.setVariable(varName, (int) newValue);
            } else {
                environment.setVariable(varName, newValue);
            }
            return true;
        }
        
        return false; // Not an increment/decrement operation
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
        try {
            Process process;
            String os = System.getProperty("os.name").toLowerCase();
            
            if (os.contains("win")) {
                // Windows - use cmd.exe to handle complex commands with pipes, etc.
                process = new ProcessBuilder("cmd", "/c", command).start();
            } else {
                // Unix-like systems (Linux, macOS, etc.) - use sh
                process = new ProcessBuilder("sh", "-c", command).start();
            }
            
            // Handle both stdout and stderr
            BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            BufferedReader stderrReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            
            // Read stdout in a separate thread
            Thread stdoutThread = new Thread(() -> {
                try {
                    String line;
                    while ((line = stdoutReader.readLine()) != null) {
                        System.out.println(line);
                    }
                    stdoutReader.close();
                }
                
                catch (IOException e) {
                    System.err.println("Error reading stdout: " + e.getMessage());
                }
            });
            
            // Read stderr in a separate thread
            Thread stderrThread = new Thread(() -> {
                try {
                    String line;
                    while ((line = stderrReader.readLine()) != null) {
                        System.err.println(line);
                    }
                    stderrReader.close();
                }
                
                catch (IOException e) {
                    System.err.println("Error reading stderr: " + e.getMessage());
                }
            });
            
            stdoutThread.start();
            stderrThread.start();
            
            // Wait for the process to complete
            int exitCode = process.waitFor();
            
            // Wait for output threads to finish
            stdoutThread.join();
            stderrThread.join();
            
            // Optional: You might want to expose the exit code somehow
            // For now, we'll just continue execution regardless of exit code
            
        }
        
        catch (IOException e) {
            throw new RuntimeException("Failed to execute system command: " + command, e);
        }
        
        catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("System command execution was interrupted: " + command, e);
        }
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
                String line = body.get(i).trim(); // Trim line once here
                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("//")) {
                    continue;
                }
                
                // Handle if statements
                if (line.startsWith("if")) {
                    // Use the Statements class to process the conditional
                    int newIndex = Statements.processConditionalStatement(body, i, new Executor(localEnv));
                    
                    // Important: Make sure we're not stuck in an infinite loop
                    if (newIndex <= i) {
                        throw new RuntimeException("Error processing if statement at line: " + line);
                    }
                    
                    i = newIndex - 1; // -1 because the loop will increment i
                    continue;
                }
                
                // Handle for loops
                if (line.startsWith("for")) {
                    // Process the for loop
                    int newIndex = ForLoop.processForLoop(body, i, new Executor(localEnv));
                    
                    // Ensure we're making progress
                    if (newIndex <= i) {
                        throw new RuntimeException("Error processing for loop at line: " + line);
                    }
                    
                    i = newIndex - 1; // -1 because the loop will increment i
                    continue;
                }
                
                // Handle while loops
                if (line.startsWith("while")) {
                    // Process the while loop
                    int newIndex = Loop.processLoop(body, i, new Executor(localEnv));
                    
                    // Ensure we're making progress
                    if (newIndex <= i) {
                        throw new RuntimeException("Error processing while loop at line: " + line);
                    }
                    
                    i = newIndex - 1; // -1 because the loop will increment i
                    continue;
                }

                // Handle switch statements
                if (line.startsWith("switch")) {
                    // Process the switch statement
                    int newIndex = Switch.processSwitchStatement(body, i, new Executor(localEnv));
                    
                    // Ensure we're making progress
                    if (newIndex <= i) {
                        throw new RuntimeException("Error processing switch statement at line: " + line);
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
                new Executor(localEnv).execute(line); // Pass the already trimmed line
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
        // Support for higher-order functions: map, filter, foldlt, foldrt
        if (functionName.equals("map")) {
            if (args.length != 2) throw new RuntimeException("map expects 2 arguments: lambda, list");
            // Lambda as string, list as comma-separated string
            String lambda = args[0];
            String listStr = args[1];
            List<Object> list = new ArrayList<>();
            for (String s : listStr.replace("[","").replace("]","").split(",")) {
                list.add(evaluate(s.trim()));
            }
            java.util.function.Function<Object, Object> fn = new Parser(new ArrayList<>()).makeUnaryLambda(lambda, this);
            return FunctionHigherOrder.map(fn, list);
        } else if (functionName.equals("filter")) {
            if (args.length != 2) throw new RuntimeException("filter expects 2 arguments: lambda, list");
            String lambda = args[0];
            String listStr = args[1];
            List<Object> list = new ArrayList<>();
            for (String s : listStr.replace("[","").replace("]","").split(",")) {
                list.add(evaluate(s.trim()));
            }
            java.util.function.Function<Object, Boolean> pred = new Parser(new ArrayList<>()).makePredicateLambda(lambda, this);
            return FunctionHigherOrder.filter(pred, list);
        } else if (functionName.equals("foldlt")) {
            if (args.length != 3) throw new RuntimeException("foldlt expects 3 arguments: lambda, initial, list");
            String lambda = args[0];
            Object initial = evaluate(args[1]);
            String listStr = args[2];
            List<Object> list = new ArrayList<>();
            for (String s : listStr.replace("[","").replace("]","").split(",")) {
                list.add(evaluate(s.trim()));
            }
            java.util.function.BiFunction<Object, Object, Object> bifn = new Parser(new ArrayList<>()).makeBinaryLambda(lambda, this);
            return FunctionHigherOrder.foldlt(bifn, initial, list);
        } else if (functionName.equals("foldrt")) {
            if (args.length != 3) throw new RuntimeException("foldrt expects 3 arguments: lambda, initial, list");
            String lambda = args[0];
            Object initial = evaluate(args[1]);
            String listStr = args[2];
            List<Object> list = new ArrayList<>();
            for (String s : listStr.replace("[","").replace("]","").split(",")) {
                list.add(evaluate(s.trim()));
            }
            java.util.function.BiFunction<Object, Object, Object> bifn = new Parser(new ArrayList<>()).makeBinaryLambda(lambda, this);
            return FunctionHigherOrder.foldrt(bifn, initial, list);
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
        Matcher matcher = FUNCTION_CALL_PATTERN.matcher(expression);
        if (matcher.matches()) {
            String functionName = matcher.group(1);
            String args = matcher.group(2).trim();
            String[] arguments = args.isEmpty() ? new String[0] : splitByCommaWithTrim(args).toArray(new String[0]);
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
