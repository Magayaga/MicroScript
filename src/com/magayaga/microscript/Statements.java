/**
 * MicroScript — The programming language
 * Copyright (c) 2025 Cyril John Magayaga
 * 
 * It was originally written in Java programming language.
 */
package com.magayaga.microscript;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Statements {
    
    /**
     * Processes conditional statements (if/elif/else blocks) in the code
     * @param lines The list of code lines to process
     * @param startIndex The starting index of the if statement
     * @param executor The executor to execute code blocks with
     * @return The index after the entire if/elif/else block
     */
    public static int processConditionalStatement(List<String> lines, int startIndex, Executor executor) {
        int currentIndex = startIndex;
        String line = lines.get(currentIndex).trim();
        
        // Process the 'if' statement
        if (line.startsWith("if")) {
            // Extract condition from if statement
            Pattern ifPattern = Pattern.compile("if\\s*\\((.+?)\\)\\s*\\{");
            Matcher ifMatcher = ifPattern.matcher(line);
            
            if (!ifMatcher.find()) {
                throw new RuntimeException("Invalid if statement syntax at line: " + line);
            }
            
            String condition = ifMatcher.group(1).trim();
            Object conditionResult = executor.evaluate(condition);
            boolean conditionValue = isTrue(conditionResult);
            
            // Find the end of the if block
            int blockEndIndex = findMatchingClosingBrace(lines, currentIndex);
            
            if (blockEndIndex == -1) {
                throw new RuntimeException("Missing closing brace for if statement starting at line: " + line);
            }
            
            // Execute the if block if condition is true
            if (conditionValue) {
                executeBlock(lines, currentIndex + 1, blockEndIndex, executor);
                // Skip to the end of the entire conditional structure
                return findEndOfConditionalStructure(lines, blockEndIndex + 1);
            }
            
            // If condition is false, move to check for elif or else
            currentIndex = blockEndIndex + 1;
            
            // Check for elif or else blocks
            while (currentIndex < lines.size()) {
                line = currentIndex < lines.size() ? lines.get(currentIndex).trim() : "";
                
                // Handle 'elif' blocks
                if (line.startsWith("elif")) {
                    Pattern elifPattern = Pattern.compile("elif\\s*\\((.+?)\\)\\s*\\{");
                    Matcher elifMatcher = elifPattern.matcher(line);
                    
                    if (!elifMatcher.find()) {
                        throw new RuntimeException("Invalid elif statement syntax at line: " + line);
                    }
                    
                    String elifCondition = elifMatcher.group(1).trim();
                    Object elifResult = executor.evaluate(elifCondition);
                    boolean elifValue = isTrue(elifResult);
                    
                    // Find the end of the elif block
                    int elifBlockEndIndex = findMatchingClosingBrace(lines, currentIndex);
                    
                    if (elifBlockEndIndex == -1) {
                        throw new RuntimeException("Missing closing brace for elif statement at line: " + line);
                    }
                    
                    // Execute the elif block if condition is true
                    if (elifValue) {
                        executeBlock(lines, currentIndex + 1, elifBlockEndIndex, executor);
                        // Skip to the end of the entire conditional structure
                        return findEndOfConditionalStructure(lines, elifBlockEndIndex + 1);
                    }
                    
                    // Move to the next block
                    currentIndex = elifBlockEndIndex + 1;
                }
                // Handle 'else' block
                else if (line.startsWith("else {") || line.equals("else{")) {
                    // Find the end of the else block
                    int elseBlockEndIndex = findMatchingClosingBrace(lines, currentIndex);
                    
                    if (elseBlockEndIndex == -1) {
                        throw new RuntimeException("Missing closing brace for else statement at line: " + line);
                    }
                    
                    // Execute the else block
                    executeBlock(lines, currentIndex + 1, elseBlockEndIndex, executor);
                    
                    // Return the index after the else block
                    return elseBlockEndIndex + 1;
                }
                else {
                    // No more elif or else blocks, return the current index
                    return currentIndex;
                }
            }
            
            // Return the index after the conditional structure
            return currentIndex;
        }
        
        // Should not reach here
        return startIndex + 1;
    }
    
    /**
     * Find the matching closing brace for a block starting with an opening brace
     * @param lines List of code lines
     * @param openingBraceLineIndex The line index containing the opening brace
     * @return The index of the line with the matching closing brace
     */
    private static int findMatchingClosingBrace(List<String> lines, int openingBraceLineIndex) {
        int braceCount = 0;
        boolean openingBraceFound = false;
        
        // Count opening brace on the first line
        String firstLine = lines.get(openingBraceLineIndex).trim();
        for (char c : firstLine.toCharArray()) {
            if (c == '{') {
                braceCount++;
                openingBraceFound = true;
            } else if (c == '}') {
                braceCount--;
            }
        }
        
        // If opening brace wasn't found on the first line, return error
        if (!openingBraceFound) {
            throw new RuntimeException("No opening brace found at line index: " + openingBraceLineIndex);
        }
        
        // Process subsequent lines
        for (int i = openingBraceLineIndex + 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            
            for (char c : line.toCharArray()) {
                if (c == '{') {
                    braceCount++;
                } else if (c == '}') {
                    braceCount--;
                    if (braceCount == 0) {
                        return i;
                    }
                }
            }
        }
        
        // No matching closing brace found
        return -1;
    }
    
    /**
     * Execute a block of code between the given line indices
     * @param lines List of code lines
     * @param startIndex Start index of the block (exclusive of the opening brace line)
     * @param endIndex End index of the block (inclusive of the closing brace line)
     * @param executor The executor to execute the lines with
     */
    private static void executeBlock(List<String> lines, int startIndex, int endIndex, Executor executor) {
        for (int i = startIndex; i < endIndex; i++) {
            String line = lines.get(i).trim();
            
            // Skip empty lines and closing brace
            if (line.isEmpty() || line.equals("}")) {
                continue;
            }
            
            // Process nested if statements
            if (line.startsWith("if")) {
                i = processConditionalStatement(lines, i, executor) - 1; // -1 because the loop will increment i
                continue;
            }
            
            // Check for return statement
            if (line.startsWith("return")) {
                // The function processing will handle returns
                executor.execute(line);
                return; // Exit block execution on return
            }
            
            // Execute the line
            executor.execute(line);
        }
    }
    
    /**
     * Find the end of a conditional structure (after all elif and else blocks)
     * @param lines List of code lines
     * @param startIndex The index to start searching from
     * @return The index after the entire conditional structure
     */
    private static int findEndOfConditionalStructure(List<String> lines, int startIndex) {
        int currentIndex = startIndex;
        
        while (currentIndex < lines.size()) {
            String line = lines.get(currentIndex).trim();
            
            if (line.startsWith("elif") || line.startsWith("else {") || line.equals("else{")) {
                // Found elif or else block, skip it
                int blockEndIndex = findMatchingClosingBrace(lines, currentIndex);
                if (blockEndIndex == -1) {
                    throw new RuntimeException("Missing closing brace for elif/else at line: " + line);
                }
                currentIndex = blockEndIndex + 1;
            } else {
                // No more elif or else blocks
                return currentIndex;
            }
        }
        
        return currentIndex;
    }
    
    /**
     * Determine if an object should be considered true in a boolean context
     * @param value The object to check
     * @return true if the object is truthy, false otherwise
     */
    private static boolean isTrue(Object value) {
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
