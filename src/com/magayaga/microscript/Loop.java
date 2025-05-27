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

public class Loop {
    
    /**
     * Main loop processing method that delegates to specific loop types
     * @param lines The list of code lines to process
     * @param startIndex The starting index of the loop statement
     * @param executor The executor to execute code blocks with
     * @return The index after the entire loop block
     */
    public static int processLoop(List<String> lines, int startIndex, Executor executor) {
        String line = lines.get(startIndex).trim();
        
        // Process different types of loops
        if (line.startsWith("while")) {
            return WhileLoop.processWhileLoop(lines, startIndex, executor);
        }
        else if (line.startsWith("for")) {
            return ForLoop.processForLoop(lines, startIndex, executor);
        }
        
        // Unknown loop type
        throw new RuntimeException("Unknown loop type at line: " + line);
    }
    
    /**
     * Execute a block of code within a loop
     * @param lines List of code lines
     * @param startIndex Start index of the block (exclusive of the opening brace line)
     * @param endIndex End index of the block (inclusive of the closing brace line)
     * @param executor The executor to execute the lines with
     * @param breakLoop Lambda function to check if the loop should break
     * @return true if the loop was broken via 'break' statement, false otherwise
     */
    public static boolean executeLoopBlock(List<String> lines, int startIndex, int endIndex, 
                                        Executor executor) {
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
            
            // Process nested loops
            if (line.startsWith("while") || line.startsWith("for")) {
                i = processLoop(lines, i, executor) - 1; // -1 because the loop will increment i
                continue;
            }
            
            // Process conditionals
            if (line.startsWith("if")) {
                i = Statements.processConditionalStatement(lines, i, executor) - 1;
                continue;
            }
            
            // Handle break statement to exit the loop
            if (line.startsWith("break")) {
                return true; // Signal to break out of the loop
            }
            
            // Handle continue statement to skip rest of the loop body
            if (line.startsWith("continue")) {
                return false; // Signal to continue to the next loop iteration
            }
            
            // Handle return statement
            if (line.startsWith("return")) {
                executor.execute(line);
                return true; // Exit loop execution on return
            }
            
            // Execute the line
            executor.execute(line);
        }
        
        return false; // Loop completed normally
    }
    
    /**
     * Find the matching closing brace for a loop or block
     * @param lines List of code lines
     * @param openingBraceLineIndex The line index containing the opening brace
     * @return The index of the line with the matching closing brace
     */
    public static int findMatchingClosingBrace(List<String> lines, int openingBraceLineIndex) {
        int braceCount = 0;
        boolean openingBraceFound = false;
        
        // Count opening brace on the first line
        String firstLine = lines.get(openingBraceLineIndex).trim();
        
        // Find the position of the opening brace
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
        }
        
        // If opening brace wasn't found on the first line, return error
        if (!openingBraceFound) {
            throw new RuntimeException("No opening brace found at line index: " + openingBraceLineIndex);
        }
        
        // Process subsequent lines
        for (int i = openingBraceLineIndex + 1; i < lines.size(); i++) {
            String line = lines.get(i);
            
            // Skip comments when counting braces
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
}