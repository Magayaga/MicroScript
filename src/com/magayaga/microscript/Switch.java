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

public class Switch {
    // Pattern to match switch statement declaration
    private static final Pattern SWITCH_PATTERN = Pattern.compile("switch\\s*\\(([^)]+)\\)\\s*\\{?");
    
    // Pattern to match case statements with various formats (fixed to handle ..= properly)
    private static final Pattern CASE_PATTERN = Pattern.compile("^\\s*(.+?)\\s*=>\\s*(.+);?\\s*,?$");
    
    // Pattern to detect multiple values separated by |
    private static final Pattern MULTI_VALUE_PATTERN = Pattern.compile("\\s*\\|\\s*");
    
    // Fixed pattern to detect range patterns with ..= operator (escape the dots)
    private static final Pattern RANGE_PATTERN = Pattern.compile("^\\s*(.+?)\\s*\\.\\.=\\s*(.+?)\\s*$");

    /**
     * Process a switch statement starting from the given index in the code lines
     * @param lines List of code lines
     * @param startIndex Starting index of the switch statement
     * @param executor Executor instance for evaluating expressions
     * @return Index after the switch statement block
     */
    public static int processSwitchStatement(List<String> lines, int startIndex, Executor executor) {
        if (startIndex >= lines.size()) {
            throw new RuntimeException("Invalid switch statement: no content");
        }

        String switchLine = lines.get(startIndex).trim();
        
        // Extract the switch expression
        Matcher switchMatcher = SWITCH_PATTERN.matcher(switchLine);
        if (!switchMatcher.find()) {
            throw new RuntimeException("Invalid switch syntax: " + switchLine);
        }
        
        String switchExpression = switchMatcher.group(1).trim();
        Object switchValue = executor.evaluate(switchExpression);
        
        // Find the opening brace if not already present
        int currentIndex = startIndex;
        boolean foundOpenBrace = switchLine.contains("{");
        
        if (!foundOpenBrace) {
            currentIndex++;
            while (currentIndex < lines.size() && !lines.get(currentIndex).trim().equals("{")) {
                currentIndex++;
            }
            if (currentIndex >= lines.size()) {
                throw new RuntimeException("Switch statement missing opening brace");
            }
        }
        
        currentIndex++; // Move past the opening brace
        
        // Process cases
        boolean caseMatched = false;
        boolean defaultFound = false;
        
        while (currentIndex < lines.size()) {
            String line = lines.get(currentIndex).trim();
            
            // Check for closing brace
            if (line.equals("}")) {
                return currentIndex + 1;
            }
            
            // Skip empty lines and comments
            if (line.isEmpty() || line.startsWith("//")) {
                currentIndex++;
                continue;
            }
            
            // Process case statement
            if (CASE_PATTERN.matcher(line).matches()) {
                Matcher caseMatcher = CASE_PATTERN.matcher(line);
                if (caseMatcher.find()) {
                    String casePattern = caseMatcher.group(1).trim();
                    String caseAction = caseMatcher.group(2).trim();
                    
                    // Remove trailing semicolon and comma if present
                    if (caseAction.endsWith(";")) {
                        caseAction = caseAction.substring(0, caseAction.length() - 1);
                    }
                    if (caseAction.endsWith(",")) {
                        caseAction = caseAction.substring(0, caseAction.length() - 1);
                    }
                    
                    // Check if this is the default case
                    if (casePattern.equals("_")) {
                        defaultFound = true;
                        if (!caseMatched) {
                            executor.execute(caseAction);
                        }
                    } else {
                        // Check if case matches
                        if (!caseMatched && matchesCase(switchValue, casePattern, executor)) {
                            executor.execute(caseAction);
                            caseMatched = true;
                        }
                    }
                }
            }
            
            currentIndex++;
        }
        
        // If no closing brace found
        throw new RuntimeException("Switch statement missing closing brace");
    }
    
    /**
     * Check if a switch value matches a case pattern
     * @param switchValue The value from the switch expression
     * @param casePattern The pattern to match against (can include | for multiple values)
     * @param executor Executor for evaluating expressions
     * @return true if the case matches
     */
    private static boolean matchesCase(Object switchValue, String casePattern, Executor executor) {
        // Handle multiple values separated by |
        if (casePattern.contains("|")) {
            String[] patterns = MULTI_VALUE_PATTERN.split(casePattern);
            for (String pattern : patterns) {
                if (matchesSingleCase(switchValue, pattern.trim(), executor)) {
                    return true;
                }
            }
            return false;
        } else {
            return matchesSingleCase(switchValue, casePattern, executor);
        }
    }
    
    /**
     * Check if a switch value matches a single case pattern
     * @param switchValue The value from the switch expression
     * @param pattern The single pattern to match against
     * @param executor Executor for evaluating expressions
     * @return true if the case matches
     */
    private static boolean matchesSingleCase(Object switchValue, String pattern, Executor executor) {
        try {
            // Check for range patterns first (e.g., 90..=94)
            Matcher rangeMatcher = RANGE_PATTERN.matcher(pattern);
            if (rangeMatcher.matches()) {
                String startExpr = rangeMatcher.group(1).trim();
                String endExpr = rangeMatcher.group(2).trim();
                
                // Evaluate range bounds
                Object startValue = executor.evaluate(startExpr);
                Object endValue = executor.evaluate(endExpr);
                
                // Range matching only works with numeric values
                if (switchValue instanceof Number && startValue instanceof Number && endValue instanceof Number) {
                    double switchNum = ((Number) switchValue).doubleValue();
                    double startNum = ((Number) startValue).doubleValue();
                    double endNum = ((Number) endValue).doubleValue();
                    
                    // Inclusive range check
                    return switchNum >= startNum && switchNum <= endNum;
                } else {
                    throw new RuntimeException("Range patterns can only be used with numeric values");
                }
            }
            
            // Regular pattern matching for non-range patterns
            Object patternValue = executor.evaluate(pattern);
            
            // Handle null values
            if (switchValue == null && patternValue == null) {
                return true;
            }
            if (switchValue == null || patternValue == null) {
                return false;
            }
            
            // Handle numeric comparisons (convert to same type for comparison)
            if (switchValue instanceof Number && patternValue instanceof Number) {
                double switchNum = ((Number) switchValue).doubleValue();
                double patternNum = ((Number) patternValue).doubleValue();
                return Math.abs(switchNum - patternNum) < 1e-10; // Handle floating point precision
            }
            
            // Handle string comparisons
            if (switchValue instanceof String && patternValue instanceof String) {
                return switchValue.equals(patternValue);
            }
            
            // Handle character comparisons
            if (switchValue instanceof Character && patternValue instanceof Character) {
                return switchValue.equals(patternValue);
            }
            
            // Handle boolean comparisons
            if (switchValue instanceof Boolean && patternValue instanceof Boolean) {
                return switchValue.equals(patternValue);
            }
            
            // Handle mixed numeric/string comparisons by converting to string
            return switchValue.toString().equals(patternValue.toString());
            
        } catch (Exception e) {
            // If pattern evaluation fails, treat as literal string comparison
            return switchValue.toString().equals(pattern);
        }
    }
    
    /**
     * Check if a line represents the start of a switch statement
     * @param line The line to check
     * @return true if the line starts a switch statement
     */
    public static boolean isSwitchStatement(String line) {
        String trimmed = line.trim();
        return SWITCH_PATTERN.matcher(trimmed).find();
    }
    
    /**
     * Extract the switch expression from a switch statement line
     * @param line The switch statement line
     * @return The expression inside the switch parentheses
     */
    public static String extractSwitchExpression(String line) {
        Matcher matcher = SWITCH_PATTERN.matcher(line.trim());
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        throw new RuntimeException("Invalid switch statement: " + line);
    }
}