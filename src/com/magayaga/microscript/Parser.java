/**
 * MicroScript — The programming language
 * Copyright (c) 2024-2025 Cyril John Magayaga
 * 
 * It was originally written in Java programming language.
 */
package com.magayaga.microscript;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private final List<String> lines;
    private final Environment environment;

    public Parser(com.magayaga.microscript.Scanner scanner) throws IOException {
        this.lines = scanner.readLines();
        this.environment = new Environment();
    }

    public Parser(List<String> lines) {
        this.lines = lines;
        this.environment = new Environment();
    }

    public void parse() {
        int i = 0;
        boolean hasCStyleMain = false;
        while (i < lines.size()) {
            String line = lines.get(i).trim();
            // Skip comments and empty lines
            if (line.startsWith("//") || line.isEmpty()) {
                i++;
                continue;
            }
            // Skip multi-line comments /** ... */ and /* ... */
            if (line.startsWith("/*")) {
                // Find the end of the multi-line comment
                while (i < lines.size() && !lines.get(i).contains("*/")) {
                    i++;
                }
                // Skip the line with the closing */ as well
                if (i < lines.size()) i++;
                continue;
            }

            // C-style function
            if (line.matches("^(String|Int32|Int64|Float32|Float64|fn)\\s+\\w+\\s*\\(.*\\)\\s*\\{")) {
                int closingBraceIndex = findClosingBrace(i);
                parseFunction(i, closingBraceIndex);
                if (line.startsWith("fn main")) hasCStyleMain = true;
                i = closingBraceIndex + 1;
            }
            
            // MicroScript-style function
            else if (line.startsWith("function ")) {
                int closingBraceIndex = findClosingBrace(i);
                parseFunction(i, closingBraceIndex);
                i = closingBraceIndex + 1;
            }
            
            // Arrow function
            else if (line.contains("=>")) {
                parseArrowFunction(line);
                i++;
            }
            
            // Handle if/elif/else chain as a single block
            else if (line.startsWith("if")) {
                Executor executor = new Executor(environment);
                int afterConditional = Statements.processConditionalStatement(lines, i, executor);
                i = afterConditional; // Skip all lines in the conditional chain
            }
            
            // Handle while loop as a single block
            else if (line.startsWith("while")) {
                Executor executor = new Executor(environment);
                int afterLoop = Loop.processLoop(lines, i, executor);
                i = afterLoop; // Skip all lines in the loop
            }

            else {
                // Execute top-level commands
                parseLine(line);
                i++;
            }
        }

        // Auto-execute C-style main if present
        if (hasCStyleMain) {
            Function mainFunc = environment.getFunction("main");
            if (mainFunc != null) {
                Executor executor = new Executor(environment);
                executor.executeFunction("main", new String[0]);
            }
        }
    }
    
    /**
     * Find the end index of an entire conditional block (including any elif/else statements)
     * @param startIndex The index of the line with the initial if statement
     * @return The index after the entire conditional block
     */
    private int findEndOfConditionalBlock(int startIndex) {
        int blockEndIndex = findClosingBrace(startIndex);
        int currentIndex = blockEndIndex + 1;
        
        while (currentIndex < lines.size()) {
            String line = lines.get(currentIndex).trim();
            
            if (line.startsWith("elif") || line.equals("else {") || line.equals("else{")) {
                // Found an elif or else block
                int nextBlockEnd = findClosingBrace(currentIndex);
                if (nextBlockEnd == -1) {
                    throw new RuntimeException("Missing closing brace for elif/else at line: " + (currentIndex + 1));
                }
                currentIndex = nextBlockEnd + 1;
            } else {
                // No more elif or else blocks
                break;
            }
        }
        
        return currentIndex;
    }
    
    /**
     * Parse an if statement and its associated blocks
     * @param startIndex The index of the line with the if statement
     * @param endIndex The index of the closing brace of the if block
     */
    private void parseIfStatement(int startIndex, int endIndex) {
        // We don't need to actually execute anything here, just validate syntax
        String ifLine = lines.get(startIndex).trim();
        
        // Verify if statement syntax
        Pattern ifPattern = Pattern.compile("if\\s*\\((.+?)\\)\\s*\\{");
        Matcher ifMatcher = ifPattern.matcher(ifLine);
        
        if (!ifMatcher.find()) {
            throw new RuntimeException("Invalid if statement syntax at line: " + (startIndex + 1));
        }
    }

    private void parseArrowFunction(String line) {
        // Format: var name = |Type: param1, Type: param2| => ReturnType {body} or expression;
        Pattern pattern = Pattern.compile("var\\s+(\\w+)\\s*=\\s*\\|(.*?)\\|\\s*=>\\s*(\\w+)?\\s*\\{(.*?)\\};");
        Matcher matcher = pattern.matcher(line);
        
        if (matcher.find()) {
            String name = matcher.group(1).trim();
            String paramString = matcher.group(2).trim();
            String returnType = matcher.group(3);
            String body = matcher.group(4).trim();
            
            // If return type is null, infer it from parameters or set to void
            if (returnType == null || returnType.isEmpty()) {
                returnType = "void";
            }
            
            List<Parameter> parameters = new ArrayList<>();
            
            // Handle the empty parameter case |&|
            if (paramString.equals("&")) {
                // No parameters
            } else {
                // Parse parameters: Float64: a, String: b, etc.
                String[] paramParts = paramString.split(",");
                for (String param : paramParts) {
                    String[] typeAndName = param.trim().split(":");
                    if (typeAndName.length != 2) {
                        throw new RuntimeException("Invalid parameter format in arrow function: " + param);
                    }
                    parameters.add(new Parameter(typeAndName[1].trim(), typeAndName[0].trim()));
                }
            }
            
            // Create ArrowFunction and define it in environment
            ArrowFunction arrowFunction = new ArrowFunction(name, parameters, returnType, body, true);
            environment.defineFunction(arrowFunction);
            
            // Also store the function as a variable
            environment.setVariable(name, arrowFunction);
        } else {
            // Try to match expression body format: var name = |params| => expression;
            Pattern exprPattern = Pattern.compile("var\\s+(\\w+)\\s*=\\s*\\|(.*?)\\|\\s*=>\\s*([^{][^;]*);");
            Matcher exprMatcher = exprPattern.matcher(line);
            
            if (exprMatcher.find()) {
                String name = exprMatcher.group(1).trim();
                String paramString = exprMatcher.group(2).trim();
                String expression = exprMatcher.group(3).trim();
                
                // Default return type (can be improved with type inference)
                String returnType = "void";
                
                // Extract return type if it's specified in the paramString using regex
                Pattern returnTypePattern = Pattern.compile("=>\\s*(\\w+)");
                Matcher returnTypeMatcher = returnTypePattern.matcher(line);
                if (returnTypeMatcher.find()) {
                    returnType = returnTypeMatcher.group(1);
                    paramString = paramString.replaceAll("=>\\s*\\w+", "").trim();
                }
                
                List<Parameter> parameters = new ArrayList<>();
                
                // Handle the empty parameter case |&|
                if (paramString.equals("&")) {
                    // No parameters
                } else {
                    // Parse parameters: Float64: a, String: b, etc.
                    String[] paramParts = paramString.split(",");
                    for (String param : paramParts) {
                        String[] typeAndName = param.trim().split(":");
                        if (typeAndName.length != 2) {
                            throw new RuntimeException("Invalid parameter format in arrow function: " + param);
                        }
                        parameters.add(new Parameter(typeAndName[1].trim(), typeAndName[0].trim()));
                    }
                }
                
                // Create ArrowFunction with expression body
                ArrowFunction arrowFunction = new ArrowFunction(name, parameters, returnType, expression, true);
                environment.defineFunction(arrowFunction);
                
                // Also store the function as a variable
                environment.setVariable(name, arrowFunction);
            } else {
                throw new RuntimeException("Invalid arrow function syntax: " + line);
            }
        }
    }

    private void parseFunction(int start, int end) {
        String header = lines.get(start).trim();
        
        // C-style function declaration regex
        Pattern cStylePattern = Pattern.compile("^(String|Int32|Int64|Float32|Float64|fn)\\s+(\\w+)\\s*\\(([^)]*)\\)\\s*\\{");
        Matcher cStyleMatcher = cStylePattern.matcher(header);
    
        // MicroScript-style function declaration regex
        Pattern microScriptPattern = Pattern.compile("function\\s+(\\w+)\\(([^)]*)\\)\\s*(->\\s*(\\w+))?\\s*\\{");
        Matcher microScriptMatcher = microScriptPattern.matcher(header);
    
        if (cStyleMatcher.matches()) {
            String returnType = cStyleMatcher.group(1);
            String name = cStyleMatcher.group(2);
            String params = cStyleMatcher.group(3).trim();
            parseFunctionBody(name, params, returnType, start, end);
        }
        
        else if (microScriptMatcher.matches()) {
            String name = microScriptMatcher.group(1);
            String params = microScriptMatcher.group(2).trim();
            String returnType = microScriptMatcher.group(4) != null ? microScriptMatcher.group(4).trim() : "void";
            parseFunctionBody(name, params, returnType, start, end);
        }
        
        else {
            throw new RuntimeException("Syntax error: Invalid function declaration.");
        }
    }
    
    private void parseFunctionBody(String name, String params, String returnType, int start, int end) {
        List<Parameter> parameters = new ArrayList<>();
        if (!params.isEmpty()) {
            for (String param : params.split("\\s*,\\s*")) {
                String[] parts = param.split(":");
                if (parts.length != 2) {
                    throw new RuntimeException("Syntax error: Invalid parameter declaration.");
                }
                parameters.add(new Parameter(parts[0].trim(), parts[1].trim()));
            }
        }
        List<String> body = new ArrayList<>();
        for (int i = start + 1; i < end; i++) {
            body.add(lines.get(i).trim());
        }

        environment.defineFunction(new Function(name, parameters, returnType, body));
    }
        
    private int findClosingBrace(int start) {
        int openBraces = 0;
        for (int i = start; i < lines.size(); i++) {
            String line = lines.get(i);
            openBraces += line.chars().filter(ch -> ch == '{').count();
            openBraces -= line.chars().filter(ch -> ch == '}').count();
            if (openBraces == 0) {
                return i;
            }
        }
        return -1;
    }

    private void parseLine(String line) {
        line = line.trim();
        if (line.startsWith("import ")) {
            String moduleName = line.substring(7).trim();
            Import.importModule(moduleName, environment);
            return;
        }

        // Defensive: Never process elif/else at top level
        if (line.startsWith("elif") || line.startsWith("else")) {
            return;
        }

        // Regex to match console.write statements
        Pattern pattern = Pattern.compile("console.write\\((.*)\\);");
        Matcher matcher = pattern.matcher(line);
        if (matcher.matches()) {
            String expression = matcher.group(1);
            Executor executor = new Executor(environment);
            executor.execute("console.write(" + expression + ")");
            return;
        }

        // Regex to match console.writef statements
        Pattern writefPattern = Pattern.compile("console.writef\\((.*)\\);");
        Matcher writefMatcher = writefPattern.matcher(line);
        if (writefMatcher.matches()) {
            String expression = writefMatcher.group(1);
            Executor executor = new Executor(environment);
            executor.execute("console.writef(" + expression + ")");
            return;
        }

        // Regex to match io::print and io::println statements
        Pattern ioPattern = Pattern.compile("io::(print|println)\\((.*)\\);");
        Matcher ioMatcher = ioPattern.matcher(line);
        if (ioMatcher.matches()) {
            String functionName = "io::" + ioMatcher.group(1);
            String args = ioMatcher.group(2).trim();
            Executor executor = new Executor(environment);
            if (args.isEmpty()) {
                executor.executeFunction(functionName, new String[0]);
            } else {
                executor.executeFunction(functionName, args.split("\\s*,\\s*"));
            }
            return;
        }

        // Function call
        Pattern callPattern = Pattern.compile("(\\w+)\\((.*)\\);");
        Matcher callMatcher = callPattern.matcher(line);
        if (callMatcher.matches()) {
            String functionName = callMatcher.group(1);
            String args = callMatcher.group(2).trim();
            Executor executor = new Executor(environment);
            if (args.isEmpty()) {
                executor.executeFunction(functionName, new String[0]);
            }
            
            else {
                executor.executeFunction(functionName, args.split("\\s*,\\s*"));
            }
            return;
        }

        // Variable or boolean declaration
        if (line.startsWith("var ") || line.startsWith("bool ")) {
            Executor executor = new Executor(environment);
            executor.execute(line);
            return;
        }
        
        else if (line.contains("=")) {
            int equalsIndex = line.indexOf('=');
            String varName = line.substring(0, equalsIndex).trim();
            String valueExpression = line.substring(equalsIndex + 1).trim().replace(";", "");
            Executor executor = new Executor(environment);
            executor.execute(varName + " = " + valueExpression);
        }
    }

    private void parseClass(int start, int end) {
        String header = lines.get(start).trim();
        Pattern classPattern = Pattern.compile("class\\s+(\\w+)\\s*\\{");
        Matcher classMatcher = classPattern.matcher(header);
        
        if (!classMatcher.find()) {
            throw new RuntimeException("Invalid class declaration syntax");
        }
        
        String className = classMatcher.group(1);
        Class newClass = new Class(className);
        
        // Parse class body
        for (int i = start + 1; i < end; i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty() || line.startsWith("//")) continue;
            
            if (line.startsWith("function ")) {
                int methodEnd = findClosingBrace(i);
                parseMethod(newClass, i, methodEnd);
                i = methodEnd;
            }
        }
        
        // Register the class in the environment BEFORE parsing continues
        environment.setVariable(className, newClass);
    }

    private void parseMethod(Class targetClass, int start, int end) {
        String header = lines.get(start).trim();
        
        // Parse method declaration: function name(params) -> returnType {
        Pattern methodPattern = Pattern.compile("function\\s+(\\w+)\\s*\\(([^)]*)\\)\\s*(->\\s*(\\w+))?\\s*\\{");
        Matcher methodMatcher = methodPattern.matcher(header);
        
        if (!methodMatcher.find()) {
            throw new RuntimeException("Invalid method declaration syntax: " + header);
        }
        
        String methodName = methodMatcher.group(1);
        String paramsStr = methodMatcher.group(2);
        String returnType = methodMatcher.group(4);
        if (returnType == null) returnType = "void";
        
        // Parse parameters
        List<Parameter> parameters = new ArrayList<>();
        if (!paramsStr.trim().isEmpty()) {
            String[] paramPairs = paramsStr.split(",");
            for (String pair : paramPairs) {
                String[] parts = pair.trim().split(":");
                if (parts.length != 2) {
                    throw new RuntimeException("Invalid parameter syntax: " + pair);
                }
                parameters.add(new Parameter(parts[0].trim(), parts[1].trim()));
            }
        }
        
        // Collect method body
        List<String> body = new ArrayList<>();
        for (int i = start + 1; i < end; i++) {
            body.add(lines.get(i));
        }
        
        // Create and return the method
        Function method = new Function(methodName, parameters, returnType, body);
        targetClass.addMethod(method);
    }

    private void parseProperty(Class targetClass, String declaration) {
        // Parse property declaration: var name: type = defaultValue
        Pattern propPattern = Pattern.compile("(var|bool)\\s+(\\w+)\\s*:\\s*(\\w+)\\s*=\\s*(.+)");
        Matcher propMatcher = propPattern.matcher(declaration);
        
        if (propMatcher.find()) {
            String name = propMatcher.group(2);
            String type = propMatcher.group(3);
            String defaultValueExpr = propMatcher.group(4).replace(";", "");
            
            Object defaultValue = new Executor(environment).evaluate(defaultValueExpr);
            Class.Property property = new Class.Property(name, type, defaultValue);
            targetClass.addProperty(property);
        } else {
            throw new RuntimeException("Invalid property declaration: " + declaration);
        }
    }

    /**
     * Creates a unary lambda function from a string expression
     */
    public java.util.function.Function<Object, Object> makeUnaryLambda(String lambda, Executor executor) {
        return (arg) -> {
            Environment localEnv = new Environment(executor.environment);
            localEnv.setVariable("it", arg);  // Use 'it' as default parameter name
            return new Executor(localEnv).evaluate(lambda);
        };
    }

    /**
     * Creates a predicate lambda function from a string expression
     */
    public java.util.function.Function<Object, Boolean> makePredicateLambda(String lambda, Executor executor) {
        return (arg) -> {
            Environment localEnv = new Environment(executor.environment);
            localEnv.setVariable("it", arg);
            Object result = new Executor(localEnv).evaluate(lambda);
            return result instanceof Boolean ? (Boolean) result : false;
        };
    }

    /**
     * Creates a binary lambda function from a string expression
     */
    public java.util.function.BiFunction<Object, Object, Object> makeBinaryLambda(String lambda, Executor executor) {
        return (arg1, arg2) -> {
            Environment localEnv = new Environment(executor.environment);
            localEnv.setVariable("acc", arg1);  // First argument is accumulator
            localEnv.setVariable("it", arg2);   // Second argument is current item
            return new Executor(localEnv).evaluate(lambda);
        };
    }
}
