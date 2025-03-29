/**
 * MicroScript — The programming language
 * Copyright (c) 2024-2025 Cyril John Magayaga
 * 
 * It was originally written in Java programming language.
 */
package com.magayaga.microscript;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser {
    private final List<String> lines;
    private final Environment environment;

    public Parser(List<String> lines) {
        this.lines = lines;
        this.environment = new Environment();
    }

    public void parse() {
        int i = 0;
        while (i < lines.size()) {
            String line = lines.get(i).trim();
            // Skip comments and empty lines
            if (line.startsWith("//") || line.isEmpty()) {
                i++;
                continue;
            }

            // Function definition
            if (line.startsWith("function ")) {
                int closingBraceIndex = findClosingBrace(i);
                if (closingBraceIndex == -1) {
                    throw new RuntimeException("Syntax error: Unmatched '{' in function definition.");
                }
                parseFunction(i, closingBraceIndex);
                i = closingBraceIndex + 1;
            }
            
            else {
                // Execute top-level commands
                parseLine(line);
                i++;
            }
        }
    }

    private void parseFunction(int start, int end) {
        String header = lines.get(start).trim();
        Matcher matcher = Pattern.compile("function\\s+(\\w+)\\(([^)]*)\\)\\s*->\\s*(\\w+)\\s*\\{").matcher(header);
        if (!matcher.matches()) {
            throw new RuntimeException("Syntax error: Invalid function declaration.");
        }

        String name = matcher.group(1);
        String params = matcher.group(2).trim();
        String returnType = matcher.group(3).trim();
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
        // Regex to match console.write statements
        Pattern pattern = Pattern.compile("console.write\\((.*)\\);");
        Matcher matcher = pattern.matcher(line);
        if (matcher.matches()) {
            String expression = matcher.group(1);
            Executor executor = new Executor(environment);
            executor.execute("console.write(" + expression + ")");
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
}
