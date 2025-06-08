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
    
    // Exception classes for loop control
    public static class BreakException extends RuntimeException {
        public BreakException() {
            super("Break statement executed");
        }
    }
    
    public static class ContinueException extends RuntimeException {
        public ContinueException() {
            super("Continue statement executed");
        }
    }
    
    /**
     * Processes conditional statements (if/elif/else blocks) in the code
     * @param lines The list of code lines to process
     * @param startIndex The starting index of the if statement
     * @param executor The executor to execute code blocks with
     * @return The index after the entire if/elif/else block
     */
    public static int processConditionalStatement(List<String> lines, int startIndex, Executor executor) {
        int currentIndex = startIndex;
        Object[] lineAndIndex = getNonEmptyNonCommentLineWithIndex(lines, currentIndex);
        String line = (String) lineAndIndex[0];
        currentIndex = (int) lineAndIndex[1];
        
        // Process the 'if' statement
        if (line != null && line.startsWith("if")) {
            // Extract condition from if statement
            Pattern ifPattern = Pattern.compile("if\\s*\\((.+?)\\)\\s*(\\{)?");
            Matcher ifMatcher = ifPattern.matcher(line);
            
            if (!ifMatcher.find()) {
                throw new RuntimeException("Invalid if statement syntax at line: " + line);
            }
            
            String condition = ifMatcher.group(1).trim();
            Object conditionResult = executor.evaluate(condition);
            boolean conditionValue = isTrue(conditionResult);
            
            // Find the opening brace for the if block
            int blockStartIndex = currentIndex;
            if (ifMatcher.group(2) == null) {
                blockStartIndex = findNextOpeningBrace(lines, currentIndex + 1);
                if (blockStartIndex == -1) {
                    throw new RuntimeException("Missing opening brace for if statement at or after line: " + line);
                }
            }
            
            // Find the end of the if block
            int blockEndIndex = findMatchingClosingBrace(lines, blockStartIndex);
            if (blockEndIndex == -1) {
                throw new RuntimeException("Missing closing brace for if statement starting at line: " + line);
            }
            
            // Execute the if block if condition is true
            if (conditionValue) {
                try {
                    executeBlock(lines, blockStartIndex + 1, blockEndIndex, executor);
                } catch (BreakException | ContinueException e) {
                    // Re-throw loop control exceptions so they can be handled by the loop
                    throw e;
                }
                // Skip to the end of the entire conditional structure
                return findEndOfConditionalStructure(lines, blockEndIndex + 1);
            }
            
            // If condition is false, move to check for elif or else
            currentIndex = blockEndIndex + 1;
            
            // Check for elif or else blocks
            while (currentIndex < lines.size()) {
                lineAndIndex = getNonEmptyNonCommentLineWithIndex(lines, currentIndex);
                line = (String) lineAndIndex[0];
                currentIndex = (int) lineAndIndex[1];
                if (line == null) break;
                
                // Handle 'elif' blocks
                if (line.startsWith("elif")) {
                    Pattern elifPattern = Pattern.compile("elif\\s*\\((.+?)\\)\\s*(\\{)?");
                    Matcher elifMatcher = elifPattern.matcher(line);
                    
                    if (!elifMatcher.find()) {
                        throw new RuntimeException("Invalid elif statement syntax at line: " + line);
                    }
                    
                    String elifCondition = elifMatcher.group(1).trim();
                    Object elifResult = executor.evaluate(elifCondition);
                    boolean elifValue = isTrue(elifResult);
                    
                    // Find the opening brace for the elif block
                    int elifBlockStartIndex = currentIndex;
                    if (elifMatcher.group(2) == null) {
                        elifBlockStartIndex = findNextOpeningBrace(lines, currentIndex + 1);
                        if (elifBlockStartIndex == -1) {
                            throw new RuntimeException("Missing opening brace for elif statement at or after line: " + line);
                        }
                    }
                    
                    // Find the end of the elif block
                    int elifBlockEndIndex = findMatchingClosingBrace(lines, elifBlockStartIndex);
                    if (elifBlockEndIndex == -1) {
                        throw new RuntimeException("Missing closing brace for elif statement at line: " + line);
                    }
                    
                    // Execute the elif block if condition is true
                    if (elifValue) {
                        try {
                            executeBlock(lines, elifBlockStartIndex + 1, elifBlockEndIndex, executor);
                        } catch (BreakException | ContinueException e) {
                            // Re-throw loop control exceptions so they can be handled by the loop
                            throw e;
                        }
                        // Skip to the end of the entire conditional structure
                        return findEndOfConditionalStructure(lines, elifBlockEndIndex + 1);
                    }
                    
                    // Move to the next block
                    currentIndex = elifBlockEndIndex + 1;
                }
                // Handle 'else' block
                else if (line.startsWith("else")) {
                    // Find the opening brace for the else block
                    int elseBlockStartIndex = currentIndex;
                    if (!line.contains("{")) {
                        elseBlockStartIndex = findNextOpeningBrace(lines, currentIndex + 1);
                        if (elseBlockStartIndex == -1) {
                            throw new RuntimeException("Missing opening brace for else statement at or after line: " + line);
                        }
                    }
                    // Find the end of the else block
                    int elseBlockEndIndex = findMatchingClosingBrace(lines, elseBlockStartIndex);
                    if (elseBlockEndIndex == -1) {
                        throw new RuntimeException("Missing closing brace for else statement at line: " + line);
                    }
                    // Execute the else block
                    try {
                        executeBlock(lines, elseBlockStartIndex + 1, elseBlockEndIndex, executor);
                    } catch (BreakException | ContinueException e) {
                        // Re-throw loop control exceptions so they can be handled by the loop
                        throw e;
                    }
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
     * Processes loop statements (for/while loops) with break/continue support
     * @param lines The list of code lines to process
     * @param startIndex The starting index of the loop statement
     * @param executor The executor to execute code blocks with
     * @return The index after the entire loop block
     */
    public static int processLoopStatement(List<String> lines, int startIndex, Executor executor) {
        int currentIndex = startIndex;
        Object[] lineAndIndex = getNonEmptyNonCommentLineWithIndex(lines, currentIndex);
        String line = (String) lineAndIndex[0];
        currentIndex = (int) lineAndIndex[1];
        
        if (line != null && (line.startsWith("for") || line.startsWith("while"))) {
            // Find the opening brace for the loop block
            int blockStartIndex = currentIndex;
            if (!line.contains("{")) {
                blockStartIndex = findNextOpeningBrace(lines, currentIndex + 1);
                if (blockStartIndex == -1) {
                    throw new RuntimeException("Missing opening brace for loop statement at or after line: " + line);
                }
            }
            
            // Find the end of the loop block
            int blockEndIndex = findMatchingClosingBrace(lines, blockStartIndex);
            if (blockEndIndex == -1) {
                throw new RuntimeException("Missing closing brace for loop statement starting at line: " + line);
            }
            
            // Execute the loop with break/continue handling
            executeLoopBlock(lines, blockStartIndex + 1, blockEndIndex, executor, line);
            
            return blockEndIndex + 1;
        }
        
        return startIndex + 1;
    }
    
    /**
     * Execute a loop block with break/continue support
     * @param lines List of code lines
     * @param startIndex Start index of the loop body
     * @param endIndex End index of the loop body
     * @param executor The executor to execute the lines with
     * @param loopDeclaration The loop declaration line (for condition checking)
     */
    private static void executeLoopBlock(List<String> lines, int startIndex, int endIndex, Executor executor, String loopDeclaration) {
        // This is a simplified example - you'll need to implement actual loop logic
        // based on your loop syntax (for/while conditions, initialization, increment, etc.)
        
        boolean isWhileLoop = loopDeclaration.startsWith("while");
        boolean isForLoop = loopDeclaration.startsWith("for");
        
        if (isWhileLoop) {
            // Extract while condition
            Pattern whilePattern = Pattern.compile("while\\s*\\((.+?)\\)");
            Matcher whileMatcher = whilePattern.matcher(loopDeclaration);
            if (!whileMatcher.find()) {
                throw new RuntimeException("Invalid while loop syntax: " + loopDeclaration);
            }
            String condition = whileMatcher.group(1).trim();
            
            // Execute while loop
            while (isTrue(executor.evaluate(condition))) {
                try {
                    executeBlock(lines, startIndex, endIndex, executor);
                } catch (BreakException e) {
                    // Break out of the loop
                    break;
                } catch (ContinueException e) {
                    // Continue to next iteration
                    continue;
                }
            }
        } else if (isForLoop) {
            // For loop implementation would go here
            // This is a placeholder - implement based on your for loop syntax
            throw new RuntimeException("For loop implementation needed");
        }
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
        
        // Check for opening brace and keep track of its position
        int openingBracePos = firstLine.indexOf('{');
        if (openingBracePos != -1) {
            braceCount = 1;
            openingBraceFound = true;
            
            // Check if there are any closing braces on the same line after the opening brace
            for (int i = openingBracePos + 1; i < firstLine.length(); i++) {
                if (firstLine.charAt(i) == '{') {
                braceCount++;
                } else if (firstLine.charAt(i) == '}') {
                braceCount--;
                    if (braceCount == 0) {
                        return openingBraceLineIndex; // The block starts and ends on the same line
                    }
                }
            }
        } else {
        // If opening brace wasn't found on the first line, return error
            throw new RuntimeException("No opening brace found at line index: " + openingBraceLineIndex);
        }
        
        // Process subsequent lines
        for (int i = openingBraceLineIndex + 1; i < lines.size(); i++) {
            String line = lines.get(i);
            
            // Skip comments
            if (line.trim().startsWith("//")) {
                continue;
            }
            
            // Handle multi-line comments
            if (line.trim().startsWith("/*")) {
                while (i < lines.size() && !lines.get(i).contains("*/")) {
                    i++;
                }
                continue;
            }
            
            // Count braces
            for (int j = 0; j < line.length(); j++) {
                char c = line.charAt(j);
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
            
            // Skip comments
            if (line.startsWith("//") || line.startsWith("/*")) {
                // Skip multi-line comments
                if (line.startsWith("/*") && !line.contains("*/")) {
                    while (i < endIndex && !lines.get(i).contains("*/")) {
                        i++;
                    }
                }
                continue;
            }
            
            // Handle break statement
            if (line.equals("break;") || line.equals("break")) {
                throw new BreakException();
            }
            
            // Handle continue statement
            if (line.equals("continue;") || line.equals("continue")) {
                throw new ContinueException();
            }
            
            // Process nested if statements - IMPORTANT: Let break/continue exceptions bubble up
            if (line.startsWith("if")) {
                try {
                    i = processConditionalStatement(lines, i, executor) - 1; // -1 because the loop will increment i
                } catch (BreakException | ContinueException e) {
                    // Re-throw these exceptions so they reach the loop that should handle them
                    throw e;
                }
                continue;
            }
            
            // Process nested loop statements
            if (line.startsWith("for") || line.startsWith("while")) {
                i = processLoopStatement(lines, i, executor) - 1; // -1 because the loop will increment i
                continue;
            }
            
            // Handle variable declaration (var ...)
            if (line.startsWith("var ")) {
                executor.execute(line);
                continue;
            }
            
            // Handle boolean declaration
            if (line.startsWith("bool ")) {
                executor.execute(line);
                continue;
            }
            
            // Handle return statement
            if (line.startsWith("return")) {
                executor.execute(line);
                return; // Exit block execution on return
            }
            
            // Handle console.write()
            if (line.startsWith("console.write")) {
                executor.execute(line);
                continue;
            }
            
            // Handle assignment statements
            if (line.contains("=") && !line.contains("==") && !line.contains("!=") && 
                !line.contains("<=") && !line.contains(">=")) {
                executor.execute(line);
                continue;
            }
            
            // Handle function calls
            if (line.contains("(") && line.contains(")") && line.endsWith(";")) {
                executor.execute(line);
                continue;
            }
            
            // Execute any other line (for assignments, function calls, etc.)
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
            Object[] lineAndIndex = getNonEmptyNonCommentLineWithIndex(lines, currentIndex);
            if (lineAndIndex[0] == null) break;
            
            String line = (String) lineAndIndex[0];
            currentIndex = (int) lineAndIndex[1];
            
            // More comprehensive check for elif/else patterns
            if (line.startsWith("elif") || 
                line.startsWith("else") && (line.contains("{") || !line.contains(";"))) {
                
                // Found elif or else block, skip it
                int blockStartIndex = currentIndex;
                if (!line.contains("{")) {
                    blockStartIndex = findNextOpeningBrace(lines, currentIndex + 1);
                    if (blockStartIndex == -1) {
                        throw new RuntimeException("Missing opening brace for elif/else at line: " + line);
                    }
                }
                
                int blockEndIndex = findMatchingClosingBrace(lines, blockStartIndex);
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

    // Helper: Get the next non-empty, non-comment line at or after index, returns [line, index] or [null, -1]
    private static Object[] getNonEmptyNonCommentLineWithIndex(List<String> lines, int index) {
        while (index < lines.size()) {
            String line = lines.get(index).trim();
            if (!line.isEmpty() && !line.startsWith("//")) {
                return new Object[]{line, index};
            }
            index++;
        }
        return new Object[]{null, -1};
    }

    // Helper: Find the next line with an opening brace '{' (skipping comments/empty lines)
    private static int findNextOpeningBrace(List<String> lines, int startIndex) {
        for (int i = startIndex; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (!line.isEmpty() && !line.startsWith("//") && line.contains("{")) {
                return i;
            }
        }
        return -1;
    }
}
