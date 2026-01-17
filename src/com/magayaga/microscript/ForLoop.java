/**
 * MicroScript — The programming language
 * Copyright (c) 2025-2026 Cyril John Magayaga
 *
 * It was originally written in Java programming language.
 */
package com.magayaga.microscript;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForLoop {

    /**
     * Process a for loop statement in the code
     * @param lines The list of code lines to process
     * @param startIndex The starting index of the for statement
     * @param executor The executor to execute code blocks with
     * @return The index after the entire for loop block
     */
    public static int processForLoop(
        List<String> lines,
        int startIndex,
        Executor executor
    ) {
        String line = lines.get(startIndex).trim();

        // Check if this is a range-based for loop (for-each style)
        if (isRangeBasedForLoop(line)) {
            return processRangeBasedForLoop(lines, startIndex, executor);
        } else {
            return processTraditionalForLoop(lines, startIndex, executor);
        }
    }

    /**
     * Check if the for loop is range-based (for-each style)
     * @param line The for loop line to check
     * @return true if it's a range-based for loop, false otherwise
     */
    private static boolean isRangeBasedForLoop(String line) {
        // First, check if it has semicolons - if so, it's traditional
        if (line.contains(";")) {
            return false;
        }

        // Pattern to match: for (type variableName : arrayName) or for (var variableName : arrayName)
        // Note: This should NOT match type annotations like "var i: Float64"
        // Range-based loops have the format: for (varDecl : collection)
        // where varDecl does NOT contain "=" and the colon is followed by a collection name
        Pattern rangePattern = Pattern.compile(
            "for\\s*\\(\\s*([^:=]+)\\s*:\\s*([^)]+)\\s*\\)"
        );
        Matcher matcher = rangePattern.matcher(line);

        if (!matcher.find()) {
            return false;
        }

        // Additional validation: the part before the colon should be a simple variable declaration
        // without assignment, and the part after should be a simple identifier (not an expression with =)
        String beforeColon = matcher.group(1).trim();
        String afterColon = matcher.group(2).trim();

        // If there's an equals sign in what comes before the colon, it's not a range-based loop
        if (beforeColon.contains("=")) {
            return false;
        }

        // If what comes after the colon contains semicolons or equals, it's not a collection reference
        if (afterColon.contains(";") || afterColon.contains("=")) {
            return false;
        }

        return true;
    }

    /**
     * Process a range-based for loop (for-each style)
     * @param lines The list of code lines to process
     * @param startIndex The starting index of the for statement
     * @param executor The executor to execute code blocks with
     * @return The index after the entire for loop block
     */
    private static int processRangeBasedForLoop(
        List<String> lines,
        int startIndex,
        Executor executor
    ) {
        String line = lines.get(startIndex).trim();

        // Parse the range-based for loop syntax
        RangeBasedForComponents components = parseRangeBasedForSyntax(line);

        // Find the opening brace for the for block
        int blockStartIndex = startIndex;
        if (!components.hasOpeningBrace) {
            blockStartIndex = findNextOpeningBrace(lines, startIndex + 1);
            if (blockStartIndex == -1) {
                throw new RuntimeException(
                    "Missing opening brace for range-based for loop at or after line: " + line
                );
            }
        }

        // Find the end of the for block
        int blockEndIndex = findMatchingClosingBrace(lines, blockStartIndex);
        if (blockEndIndex == -1) {
            throw new RuntimeException(
                "Missing closing brace for range-based for loop starting at line: " + line
            );
        }

        // Execute the range-based for loop
        executeRangeBasedForLoop(
            components.variableDeclaration,
            components.arrayName,
            lines,
            blockStartIndex + 1,
            blockEndIndex,
            executor
        );

        // Return the index after the for block
        return blockEndIndex + 1;
    }

    /**
     * Process a traditional for loop (C-style)
     * @param lines The list of code lines to process
     * @param startIndex The starting index of the for statement
     * @param executor The executor to execute code blocks with
     * @return The index after the entire for loop block
     */
    private static int processTraditionalForLoop(
        List<String> lines,
        int startIndex,
        Executor executor
    ) {
        String line = lines.get(startIndex).trim();

        // Extract for loop components from the statement
        // Support both: for (var varName: Type = initValue; condition; increment)
        // and: for (existingVar = initValue; condition; increment)

        ForLoopComponents components = parseForLoopSyntax(line);

        int blockStartIndex = startIndex;
        if (!components.hasOpeningBrace) {
            blockStartIndex = findNextOpeningBrace(lines, startIndex + 1);
            if (blockStartIndex == -1) {
                throw new RuntimeException(
                    "Missing opening brace for for loop at or after line: " + line
                );
            }
        }

        // Find the end of the for block
        int blockEndIndex = findMatchingClosingBrace(lines, blockStartIndex);
        if (blockEndIndex == -1) {
            throw new RuntimeException(
                "Missing closing brace for for loop starting at line: " + line
            );
        }

        // Execute the for loop
        executeForLoop(
            components.initialization,
            components.condition,
            components.increment,
            components.isNewVariable,
            lines,
            blockStartIndex + 1,
            blockEndIndex,
            executor
        );

        return blockEndIndex + 1;
    }

    private static class RangeBasedForComponents {
        String variableDeclaration;
        String arrayName;
        boolean hasOpeningBrace;

        RangeBasedForComponents(
            String variableDeclaration,
            String arrayName,
            boolean hasOpeningBrace
        ) {
            this.variableDeclaration = variableDeclaration;
            this.arrayName = arrayName;
            this.hasOpeningBrace = hasOpeningBrace;
        }
    }

    /**
     * Helper class to hold parsed for loop components
     */
    private static class ForLoopComponents {
        String initialization;
        String condition;
        String increment;
        boolean isNewVariable;
        boolean hasOpeningBrace;

        ForLoopComponents(
            String initialization,
            String condition,
            String increment,
            boolean isNewVariable,
            boolean hasOpeningBrace
        ) {
            this.initialization = initialization;
            this.condition = condition;
            this.increment = increment;
            this.isNewVariable = isNewVariable;
            this.hasOpeningBrace = hasOpeningBrace;
        }
    }

    private static RangeBasedForComponents parseRangeBasedForSyntax(String line) {
        Pattern rangePattern = Pattern.compile(
            "for\\s*\\(\\s*([^:]+)\\s*:\\s*([^)]+)\\s*\\)\\s*(\\{)?"
        );
        Matcher rangeMatcher = rangePattern.matcher(line);

        if (!rangeMatcher.find()) {
            throw new RuntimeException(
                "Invalid range-based for loop syntax at line: " + line
            );
        }

        String variableDeclaration = rangeMatcher.group(1).trim();
        String arrayName = rangeMatcher.group(2).trim();
        boolean hasOpeningBrace = rangeMatcher.group(3) != null;

        return new RangeBasedForComponents(
            variableDeclaration,
            arrayName,
            hasOpeningBrace
        );
    }

    /**
     * Parse for loop syntax and extract components
     * @param line The for loop line to parse
     * @return ForLoopComponents containing parsed information
     */
    private static ForLoopComponents parseForLoopSyntax(String line) {
        Pattern forPattern = Pattern.compile(
            "for\\s*\\(\\s*([^;]+)\\s*;\\s*([^;]+)\\s*;\\s*([^)]+)\\s*\\)\\s*(\\{)?"
        );
        Matcher forMatcher = forPattern.matcher(line);

        if (!forMatcher.find()) {
            throw new RuntimeException(
                "Invalid for loop syntax at line: " + line
            );
        }

        String initialization = forMatcher.group(1).trim();
        String condition = forMatcher.group(2).trim();
        String increment = forMatcher.group(3).trim();
        boolean hasOpeningBrace = forMatcher.group(4) != null;

        // Check if this is a new variable declaration
        boolean isNewVariable = initialization.startsWith("var ");

        return new ForLoopComponents(
            initialization,
            condition,
            increment,
            isNewVariable,
            hasOpeningBrace
        );
    }

    /**
     * Execute a range-based for loop (for-each style)
     * @param variableDeclaration The loop variable declaration (e.g., "int item" or "var item: Integer")
     * @param arrayName The name of the array/collection to iterate over
     * @param lines List of code lines
     * @param startIndex Start index of the loop body
     * @param endIndex End index of the loop body
     * @param executor The executor to execute the loop with
     */
    private static void executeRangeBasedForLoop(
        String variableDeclaration,
        String arrayName,
        List<String> lines,
        int startIndex,
        int endIndex,
        Executor executor
    ) {
        final int MAX_ITERATIONS = 1000000;
        int iterations = 0;

        try {
            // Get the array/collection from the executor
            Object arrayObject;
            try {
                arrayObject = executor.evaluate(arrayName);
            } catch (Exception e) {
                throw new RuntimeException(
                    "Error evaluating array/collection '" + arrayName + "': " + e.getMessage()
                );
            }

            if (arrayObject == null) {
                throw new RuntimeException("Array/collection '" + arrayName + "' is null");
            }

            // Convert to iterable (this will depend on your MicroScript type system)
            Iterable<?> iterable = convertToIterable(arrayObject);
            if (iterable == null) {
                throw new RuntimeException("'" + arrayName + "' is not iterable");
            }

            // Extract variable name from declaration
            String variableName = extractVariableName(variableDeclaration);

            // Iterate over each element
            for (Object element : iterable) {
                // Safety check for infinite loops
                if (iterations >= MAX_ITERATIONS) {
                    throw new RuntimeException(
                        "Possible infinite loop detected: exceeded " + MAX_ITERATIONS + " iterations"
                    );
                }

                // Assign the current element to the loop variable
                try {
                    executor.execute(
                        variableName + " = " + formatValueForAssignment(element)
                    );
                } catch (Exception e) {
                    throw new RuntimeException(
                        "Error assigning loop variable '" + variableName + "': " + e.getMessage()
                    );
                }

                try {
                    executeLoopBlock(lines, startIndex, endIndex, executor);
                } catch (Statements.BreakException e) {
                    break;
                } catch (Statements.ContinueException e) {
                    iterations++;
                    continue;
                }

                iterations++;
            }
        } catch (Statements.BreakException | Statements.ContinueException e) {
            // These should have been caught in the inner loop
        } catch (Exception e) {
            throw new RuntimeException(
                "Error in range-based for loop execution: " + e.getMessage()
            );
        }
    }

    /**
     * Convert an object to an Iterable for range-based for loops
     * This method should be adapted based on your MicroScript type system
     * @param arrayObject The object to convert
     * @return An Iterable, or null if conversion is not possible
     */
    private static Iterable<?> convertToIterable(Object arrayObject) {
        if (arrayObject instanceof Iterable) {
            return (Iterable<?>) arrayObject;
        }

        // Handle arrays
        if (arrayObject.getClass().isArray()) {
            // Convert array to list for iteration
            if (arrayObject instanceof Object[]) {
                return java.util.Arrays.asList((Object[]) arrayObject);
            } else if (arrayObject instanceof int[]) {
                int[] intArray = (int[]) arrayObject;
                java.util.List<Integer> list = new java.util.ArrayList<>();
                for (int value : intArray) {
                    list.add(value);
                }
                return list;
            } else if (arrayObject instanceof double[]) {
                double[] doubleArray = (double[]) arrayObject;
                java.util.List<Double> list = new java.util.ArrayList<>();
                for (double value : doubleArray) {
                    list.add(value);
                }
                return list;
            } else if (arrayObject instanceof boolean[]) {
                boolean[] boolArray = (boolean[]) arrayObject;
                java.util.List<Boolean> list = new java.util.ArrayList<>();
                for (boolean value : boolArray) {
                    list.add(value);
                }
                return list;
            }
            // Add more primitive array types as needed
        }

        // Handle strings as character sequences
        if (arrayObject instanceof String) {
            String str = (String) arrayObject;
            java.util.List<Character> chars = new java.util.ArrayList<>();
            for (char c : str.toCharArray()) {
                chars.add(c);
            }
            return chars;
        }

        return null;
    }

    /**
     * Extract the variable name from a variable declaration
     * @param variableDeclaration The variable declaration (e.g., "int item", "var item: Integer")
     * @return The variable name
     */
    private static String extractVariableName(String variableDeclaration) {
        // Handle MicroScript-style declarations: "var name: Type"
        if (variableDeclaration.startsWith("var ")) {
            String withoutVar = variableDeclaration.substring(4).trim();
            int colonIndex = withoutVar.indexOf(':');
            if (colonIndex != -1) {
                return withoutVar.substring(0, colonIndex).trim();
            } else {
                return withoutVar;
            }
        }

        // Handle C-style declarations: "type name"
        String[] parts = variableDeclaration.trim().split("\\s+");
        if (parts.length >= 2) {
            return parts[parts.length - 1];
        }

        // If it's just a name without type
        return variableDeclaration.trim();
    }

    /**
     * Format a value for assignment in MicroScript syntax
     * @param value The value to format
     * @return The formatted value as a string
     */
    private static String formatValueForAssignment(Object value) {
        if (value == null) {
            return "null";
        } else if (value instanceof String) {
            return "\"" + value.toString().replace("\"", "\\\"") + "\"";
        } else if (value instanceof Character) {
            return "'" + value.toString().replace("'", "\\'") + "'";
        } else {
            return value.toString();
        }
    }

    /**
     * Execute a for loop
     * @param initialization The initialization statement (variable declaration or assignment)
     * @param condition The loop condition to evaluate
     * @param increment The increment expression
     * @param isNewVariable Whether this loop declares a new variable
     * @param lines List of code lines
     * @param startIndex Start index of the loop body
     * @param endIndex End index of the loop body
     * @param executor The executor to execute the loop with
     */
    private static void executeForLoop(
        String initialization,
        String condition,
        String increment,
        boolean isNewVariable,
        List<String> lines,
        int startIndex,
        int endIndex,
        Executor executor
    ) {
        // Set a reasonable maximum iteration count to prevent infinite loops
        final int MAX_ITERATIONS = 1000000;
        int iterations = 0;

        try {
            // Initialize the loop variable
            // Make sure the executor can handle variable declarations properly
            if (isNewVariable) {
                // For variable declarations like "var i: Float64 = 0"
                // The executor should handle this as a declaration, not an evaluation
                executor.execute(initialization);
            } else {
                // For assignments like "i = 0" to existing variables
                executor.execute(initialization);
            }

            // For loop execution
            while (iterations < MAX_ITERATIONS) {
                // Evaluate the condition
                Object conditionResult;
                try {
                    conditionResult = executor.evaluate(condition);
                } catch (Exception e) {
                    throw new RuntimeException(
                        "Error evaluating for loop condition '" + condition + "': " + e.getMessage()
                    );
                }

                boolean conditionValue = isTruthyValue(conditionResult);
                if (!conditionValue) {
                    break;
                }

                try {
                    executeLoopBlock(lines, startIndex, endIndex, executor);
                } catch (Statements.BreakException e) {
                    break;
                } catch (Statements.ContinueException e) {
                    // Execute increment and continue
                    try {
                        executor.execute(increment);
                    } catch (Exception ex) {
                        throw new RuntimeException(
                            "Error executing for loop increment '" + increment + "': " + ex.getMessage()
                        );
                    }
                    iterations++;
                    continue;
                }

                try {
                    executor.execute(increment);
                } catch (Exception e) {
                    throw new RuntimeException(
                        "Error executing for loop increment '" + increment + "': " + e.getMessage()
                    );
                }

                // Increment iteration count to prevent infinite loops
                iterations++;

                // Safety check for infinite loops
                if (iterations >= MAX_ITERATIONS) {
                    throw new RuntimeException(
                        "Possible infinite loop detected: exceeded " + MAX_ITERATIONS + " iterations"
                    );
                }
            }
        } catch (Statements.BreakException | Statements.ContinueException e) {
            // These should have been caught in the inner loop
        } catch (Exception e) {
            throw new RuntimeException(
                "Error in for loop execution: " + e.getMessage()
            );
        }
    }

    private static void executeLoopBlock(
        List<String> lines,
        int startIndex,
        int endIndex,
        Executor executor
    ) throws Statements.BreakException, Statements.ContinueException {
        for (int i = startIndex; i < endIndex; i++) {
            String line = lines.get(i).trim();

            if (line.isEmpty() || line.startsWith("//")) {
                continue;
            }

            // Skip closing braces - they're just block delimiters
            if (line.equals("}")) {
                continue;
            }

            // Handle multi-line comments
            if (line.startsWith("/*")) {
                while (i < endIndex && !lines.get(i).contains("*/")) {
                    i++;
                }
                continue;
            }

            // Handle break - throw exception to propagate up
            if (line.equals("break") || line.equals("break;")) {
                throw new Statements.BreakException();
            }

            // Handle continue - throw exception to propagate up
            if (line.equals("continue") || line.equals("continue;")) {
                throw new Statements.ContinueException();
            }

            // Handle return statements
            if (line.startsWith("return")) {
                executor.execute(line);
                throw new Statements.BreakException(); // Treat return as break for loop purposes
            }

            if (line.startsWith("if")) {
                try {
                    int newIndex = Statements.processConditionalStatement(lines, i, executor);
                    if (newIndex > i) {
                        i = newIndex - 1;
                    }
                } catch (Statements.BreakException | Statements.ContinueException e) {
                    // Propagate break/continue from nested if
                    throw e;
                } catch (Exception e) {
                    throw new RuntimeException(
                        "Error processing if statement in for loop: " + e.getMessage()
                    );
                }
                continue;
            }

            // Handle nested while loops
            if (line.startsWith("while")) {
                try {
                    int newIndex = WhileLoop.processWhileLoop(lines, i, executor);
                    if (newIndex > i) {
                        i = newIndex - 1;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(
                        "Error processing nested while loop in for loop: " + e.getMessage()
                    );
                }
                continue;
            }

            // Handle nested for loops
            if (line.startsWith("for")) {
                try {
                    // Process the nested for loop
                    int newIndex = processForLoop(lines, i, executor);

                    // Make sure we advance properly
                    if (newIndex > i) {
                        i = newIndex - 1;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(
                        "Error processing nested for loop: " + e.getMessage()
                    );
                }
                continue;
            }

            try {
                // Execute regular statements (including variable assignments, function calls, etc.)
                executor.execute(line);
            } catch (Exception e) {
                throw new RuntimeException(
                    "Error executing statement in for loop: '" + line + "' - " + e.getMessage()
                );
            }
        }
    }

    /**
     * Find the next line with an opening brace '{' (skipping comments/empty lines)
     * @param lines List of code lines
     * @param startIndex The index to start looking from
     * @return The index of the line with an opening brace, or -1 if not found
     */
    private static int findNextOpeningBrace(
        List<String> lines,
        int startIndex
    ) {
        for (int i = startIndex; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (!line.isEmpty() && !line.startsWith("//") && line.contains("{")) {
                return i;
            }
        }
        return -1;
    }

    private static int findMatchingClosingBrace(List<String> lines, int openBraceIndex) {
        int braceCount = 0;
        boolean foundOpenBrace = false;

        for (int i = openBraceIndex; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            // Skip empty lines and comments
            if (line.isEmpty() || line.startsWith("//")) {
                continue;
            }

            // Handle multi-line comments
            if (line.startsWith("/*")) {
                while (i < lines.size() && !lines.get(i).contains("*/")) {
                    i++;
                }
                continue;
            }

            // Count braces in the line
            for (char c : line.toCharArray()) {
                if (c == '{') {
                    braceCount++;
                    foundOpenBrace = true;
                } else if (c == '}') {
                    braceCount--;
                    if (foundOpenBrace && braceCount == 0) {
                        return i;
                    }
                }
            }
        }

        return -1;
    }

    /**
     * Determine if an object should be considered truthy for loop conditions
     * @param value The object to check
     * @return true if the object is truthy, false otherwise
     */
    private static boolean isTruthyValue(Object value) {
        if (value == null) {
            return false;
        }

        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        if (value instanceof Number) {
            // Consider non-zero numbers as true
            double numValue = ((Number) value).doubleValue();
            return Math.abs(numValue) > 0.0001;
        }

        if (value instanceof String) {
            // Consider non-empty strings as true
            return !((String) value).isEmpty();
        }

        // For any other object, consider it true
        return true;
    }
}
