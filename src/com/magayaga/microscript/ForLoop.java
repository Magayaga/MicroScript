/**
 * MicroScript — The programming language
 * Copyright (c) 2025 Cyril John Magayaga
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
    public static int processForLoop(List<String> lines, int startIndex, Executor executor) {
        String line = lines.get(startIndex).trim();
        
        // Extract for loop components from the statement
        // Support both: for (var varName: Type = initValue; condition; increment)
        // and: for (existingVar = initValue; condition; increment)
        
        ForLoopComponents components = parseForLoopSyntax(line);
        
        String initialization = components.initialization;
        String condition = components.condition;
        String increment = components.increment;
        
        // Find the opening brace for the for block
        int blockStartIndex = startIndex;
        if (!components.hasOpeningBrace) {
            blockStartIndex = findNextOpeningBrace(lines, startIndex + 1);
            if (blockStartIndex == -1) {
                throw new RuntimeException("Missing opening brace for for loop at or after line: " + line);
            }
        }
        
        // Find the end of the for block
        int blockEndIndex = findMatchingClosingBrace(lines, blockStartIndex);
        if (blockEndIndex == -1) {
            throw new RuntimeException("Missing closing brace for for loop starting at line: " + line);
        }
        
        // Execute the for loop
        executeForLoop(initialization, condition, increment, components.isNewVariable,
                      lines, blockStartIndex + 1, blockEndIndex, executor);
        
        // Return the index after the for block
        return blockEndIndex + 1;
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
        
        ForLoopComponents(String initialization, String condition, String increment, 
                         boolean isNewVariable, boolean hasOpeningBrace) {
            this.initialization = initialization;
            this.condition = condition;
            this.increment = increment;
            this.isNewVariable = isNewVariable;
            this.hasOpeningBrace = hasOpeningBrace;
        }
    }
    
    /**
     * Parse for loop syntax and extract components
     * @param line The for loop line to parse
     * @return ForLoopComponents containing parsed information
     */
    private static ForLoopComponents parseForLoopSyntax(String line) {
        // More flexible pattern that captures the entire for loop structure
        Pattern forPattern = Pattern.compile("for\\s*\\(\\s*([^;]+)\\s*;\\s*([^;]+)\\s*;\\s*([^)]+)\\s*\\)\\s*(\\{)?");
        Matcher forMatcher = forPattern.matcher(line);
        
        if (!forMatcher.find()) {
            throw new RuntimeException("Invalid for loop syntax at line: " + line);
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
    private static void executeForLoop(String initialization, String condition, String increment,
                                      boolean isNewVariable, List<String> lines, int startIndex, 
                                      int endIndex, Executor executor) {
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
                    throw new RuntimeException("Error evaluating for loop condition '" + condition + "': " + e.getMessage());
                }
                
                boolean conditionValue = isTruthyValue(conditionResult);
                
                // If condition is false, exit the loop
                if (!conditionValue) {
                    break;
                }
                
                // Execute the loop body
                LoopControl loopControl = executeLoopBlock(lines, startIndex, endIndex, executor);
                
                // Handle loop control statements
                if (loopControl == LoopControl.BREAK) {
                    break;
                } else if (loopControl == LoopControl.CONTINUE) {
                    // Execute increment and continue to next iteration
                    try {
                        executor.execute(increment);
                    } catch (Exception e) {
                        throw new RuntimeException("Error executing for loop increment '" + increment + "': " + e.getMessage());
                    }
                    iterations++;
                    continue;
                } else if (loopControl == LoopControl.RETURN) {
                    // Return statement encountered, exit the loop
                    break;
                }
                
                // Execute the increment statement
                try {
                    executor.execute(increment);
                } catch (Exception e) {
                    throw new RuntimeException("Error executing for loop increment '" + increment + "': " + e.getMessage());
                }
                
                // Increment iteration count to prevent infinite loops
                iterations++;
                
                // Safety check for infinite loops
                if (iterations >= MAX_ITERATIONS) {
                    throw new RuntimeException("Possible infinite loop detected: exceeded " + 
                                             MAX_ITERATIONS + " iterations");
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error in for loop execution: " + e.getMessage());
        }
    }
    
    /**
     * Enum to represent loop control flow
     */
    private enum LoopControl {
        CONTINUE,   // Continue to next iteration
        BREAK,      // Break out of the loop
        RETURN,     // Return from function
        NORMAL      // Normal execution
    }
    
    /**
     * Execute a block of code within the loop
     * @param lines List of code lines
     * @param startIndex Start index of the block
     * @param endIndex End index of the block
     * @param executor The executor to use
     * @return LoopControl indicating how the loop should proceed
     */
    private static LoopControl executeLoopBlock(List<String> lines, int startIndex, int endIndex, Executor executor) {
        for (int i = startIndex; i < endIndex; i++) {
            String line = lines.get(i).trim();
            
            // Skip empty lines and comments
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
            
            // Handle break statements
            if (line.equals("break") || line.equals("break;")) {
                return LoopControl.BREAK;
            }
            
            // Handle continue statements
            if (line.equals("continue") || line.equals("continue;")) {
                return LoopControl.CONTINUE;
            }
            
            // Handle return statements
            if (line.startsWith("return")) {
                try {
                    executor.execute(line);
                } catch (Exception e) {
                    throw new RuntimeException("Error executing return statement in for loop: " + e.getMessage());
                }
                return LoopControl.RETURN;
            }
            
            // Handle nested if statements
            if (line.startsWith("if")) {
                try {
                    // Process the conditional statement and get the new index
                    int newIndex = Statements.processConditionalStatement(lines, i, executor);
                    
                    // Make sure we advance properly
                    if (newIndex > i) {
                        i = newIndex - 1; // -1 because the for loop will increment i
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error processing if statement in for loop: " + e.getMessage());
                }
                continue;
            }
            
            // Handle nested while loops
            if (line.startsWith("while")) {
                try {
                    // Process the nested while loop
                    int newIndex = WhileLoop.processWhileLoop(lines, i, executor);
                    
                    // Make sure we advance properly
                    if (newIndex > i) {
                        i = newIndex - 1; // -1 because the for loop will increment i
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error processing nested while loop in for loop: " + e.getMessage());
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
                        i = newIndex - 1; // -1 because the for loop will increment i
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error processing nested for loop: " + e.getMessage());
                }
                continue;
            }
            
            try {
                // Execute regular statements (including variable assignments, function calls, etc.)
                executor.execute(line);
            } catch (Exception e) {
                throw new RuntimeException("Error executing statement in for loop: '" + line + "' - " + e.getMessage());
            }
        }
        
        return LoopControl.NORMAL; // Normal execution completed
    }
    
    /**
     * Find the next line with an opening brace '{' (skipping comments/empty lines)
     * @param lines List of code lines
     * @param startIndex The index to start looking from
     * @return The index of the line with an opening brace, or -1 if not found
     */
    private static int findNextOpeningBrace(List<String> lines, int startIndex) {
        for (int i = startIndex; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (!line.isEmpty() && !line.startsWith("//") && line.contains("{")) {
                return i;
            }
        }
        return -1;
    }
    
    /**
     * Find the matching closing brace for an opening brace
     * @param lines List of code lines
     * @param openBraceIndex Index of the line with the opening brace
     * @return Index of the line with the matching closing brace, or -1 if not found
     */
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
                        return i; // Found matching closing brace
                    }
                }
            }
        }
        
        return -1; // No matching closing brace found
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
            return Math.abs(numValue) > 0.0001; // Account for floating point imprecision
        }
        
        if (value instanceof String) {
            // Consider non-empty strings as true
            return !((String) value).isEmpty();
        }
        
        // For any other object, consider it true
        return true;
    }
}
