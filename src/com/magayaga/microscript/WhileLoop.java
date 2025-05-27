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

public class WhileLoop {
    
    /**
     * Process a while loop statement in the code
     * @param lines The list of code lines to process
     * @param startIndex The starting index of the while statement
     * @param executor The executor to execute code blocks with
     * @return The index after the entire while loop block
     */
    public static int processWhileLoop(List<String> lines, int startIndex, Executor executor) {
        String line = lines.get(startIndex).trim();
        
        // Extract condition from while statement
        Pattern whilePattern = Pattern.compile("while\\s*\\((.+?)\\)\\s*(\\{)?");
        Matcher whileMatcher = whilePattern.matcher(line);
        
        if (!whileMatcher.find()) {
            throw new RuntimeException("Invalid while loop syntax at line: " + line);
        }
        
        // Extract the condition from the while statement
        String condition = whileMatcher.group(1).trim();
        
        // Find the opening brace for the while block
        int blockStartIndex = startIndex;
        if (whileMatcher.group(2) == null) {
            blockStartIndex = findNextOpeningBrace(lines, startIndex + 1);
            if (blockStartIndex == -1) {
                throw new RuntimeException("Missing opening brace for while loop at or after line: " + line);
            }
        }
        
        // Find the end of the while block
        int blockEndIndex = findMatchingClosingBrace(lines, blockStartIndex);
        if (blockEndIndex == -1) {
            throw new RuntimeException("Missing closing brace for while loop starting at line: " + line);
        }
        
        // Execute the while loop
        executeWhileLoop(lines, condition, blockStartIndex + 1, blockEndIndex, executor);
        
        // Return the index after the while block
        return blockEndIndex + 1;
    }
    
    /**
     * Execute a while loop
     * @param lines List of code lines
     * @param condition The loop condition to evaluate
     * @param startIndex Start index of the loop body
     * @param endIndex End index of the loop body
     * @param executor The executor to execute the loop with
     */
    private static void executeWhileLoop(List<String> lines, String condition, 
                                         int startIndex, int endIndex, Executor executor) {
        // Set a reasonable maximum iteration count to prevent infinite loops
        final int MAX_ITERATIONS = 1000000;
        int iterations = 0;
        
        // While loop execution
        while (iterations < MAX_ITERATIONS) {
            // Evaluate the condition
            Object conditionResult = executor.evaluate(condition);
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
                // Continue to next iteration
                iterations++;
                continue;
            } else if (loopControl == LoopControl.RETURN) {
                // Return statement encountered, exit the loop
                break;
            }
            
            // Increment iteration count to prevent infinite loops
            iterations++;
            
            // Safety check for infinite loops
            if (iterations >= MAX_ITERATIONS) {
                throw new RuntimeException("Possible infinite loop detected: exceeded " + 
                                         MAX_ITERATIONS + " iterations");
            }
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
                executor.execute(line);
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
                    throw new RuntimeException("Error processing if statement in while loop: " + e.getMessage());
                }
                continue;
            }
            
            // Handle nested while loops
            if (line.startsWith("while")) {
                try {
                    // Process the nested while loop
                    int newIndex = processWhileLoop(lines, i, executor);
                    
                    // Make sure we advance properly
                    if (newIndex > i) {
                        i = newIndex - 1; // -1 because the for loop will increment i
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Error processing nested while loop: " + e.getMessage());
                }
                continue;
            }
            
            // Handle for loops (if you have them)
            if (line.startsWith("for")) {
                try {
                    // Assuming you have a ForLoop class similar to WhileLoop
                    // int newIndex = ForLoop.processForLoop(lines, i, executor);
                    // if (newIndex > i) {
                    //     i = newIndex - 1;
                    // }
                    throw new RuntimeException("For loops not yet implemented in while loop context");
                } catch (Exception e) {
                    throw new RuntimeException("Error processing for loop in while loop: " + e.getMessage());
                }
            }
            
            try {
                // Execute regular statements (including variable assignments, increments/decrements, etc.)
                executor.execute(line);
            } catch (Exception e) {
                throw new RuntimeException("Error executing statement in while loop: '" + line + "' - " + e.getMessage());
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
