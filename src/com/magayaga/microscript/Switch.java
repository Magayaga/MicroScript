/**
 * MicroScript — The programming language
 * Copyright (c) 2025 Cyril John Magayaga
 * 
 * It was originally written in Java programming language.
 */
package com.magayaga.microscript;

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Switch {
    // Updated patterns to match MicroScript syntax without semicolons after case actions
    private static final Pattern SWITCH_PATTERN = Pattern.compile("switch\\s*\\(([^)]+)\\)\\s*");
    private static final Pattern CASE_PATTERN = Pattern.compile("^\\s*(\\d+(?:\\.\\d+)?)\\s*=>\\s*(.+?)\\s*$");
    
    public static int processSwitchStatement(List<String> lines, int startIndex, Executor executor) {
        String switchLine = lines.get(startIndex).trim();
        
        // Parse the switch expression
        Matcher switchMatcher = SWITCH_PATTERN.matcher(switchLine);
        if (!switchMatcher.find()) {
            throw new RuntimeException("Invalid switch statement syntax: " + switchLine);
        }
        
        String switchExpression = switchMatcher.group(1).trim();
        Object switchValue;
        
        try {
            switchValue = executor.evaluate(switchExpression);
        } catch (Exception e) {
            throw new RuntimeException("Failed to evaluate switch expression: " + switchExpression, e);
        }
        
        // Validate that switch value is numeric
        if (!(switchValue instanceof Number)) {
            throw new RuntimeException("Switch expression must evaluate to a numeric value, got: " + 
                                     (switchValue != null ? switchValue.getClass().getSimpleName() : "null"));
        }
        
        // Process cases in consecutive lines
        int currentIndex = startIndex + 1;
        boolean foundMatch = false;
        
        while (currentIndex < lines.size()) {
            String line = lines.get(currentIndex).trim();
            
            // Skip empty lines and comments
            if (line.isEmpty() || line.startsWith("//")) {
                currentIndex++;
                continue;
            }
            
            // Check if we've reached the end of switch cases or block
            if (line.equals("}") || line.startsWith("}") || 
                (!line.contains("=>") && !line.matches("^\\s*\\d+.*"))) {
                break;
            }
            
            // Skip lines that are clearly not case statements
            if (line.equals("{") || line.startsWith("function") || 
                line.startsWith("var ") || line.startsWith("return")) {
                break;
            }
            
            // Parse case statement
            Matcher caseMatcher = CASE_PATTERN.matcher(line);
            if (caseMatcher.matches()) {
                String caseValueStr = caseMatcher.group(1).trim();
                String action = caseMatcher.group(2).trim();
                
                try {
                    // Convert caseValue to numeric for comparison
                    double numericCaseValue = Double.parseDouble(caseValueStr);
                    double numericSwitchValue = ((Number) switchValue).doubleValue();
                    
                    // Use proper numeric comparison with epsilon for floating point
                    if (isEqual(numericCaseValue, numericSwitchValue)) {
                        // Execute the action for matching case
                        try {
                            executor.execute(action);
                            foundMatch = true;
                            // In MicroScript, we execute the first match and continue (no break statement)
                            // If you want break behavior, add a break flag and exit loop here
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to execute case action: " + action, e);
                        }
                    }
                } catch (NumberFormatException e) {
                    throw new RuntimeException("Invalid numeric case value: " + caseValueStr, e);
                }
            } else {
                // If it's not a valid case statement and contains '=>', it might be malformed
                if (line.contains("=>")) {
                    throw new RuntimeException("Invalid case statement syntax: " + line);
                }
                // Otherwise, we've reached the end of the switch block
                break;
            }
            
            currentIndex++;
        }
        
        // Optional: Handle default case or no match found
        if (!foundMatch) {
            // Could add default case handling here if needed
            // For now, just continue execution
        }
        
        return currentIndex;
    }

    /**
     * Enhanced equality check for numeric values with proper floating point comparison
     */
    private static boolean isEqual(double a, double b) {
        // Handle exact integer matches
        if (a == b) return true;
        
        // Handle floating point comparison with appropriate epsilon
        return Math.abs(a - b) < 1e-10;
    }
    
    /**
     * General equality check for objects (kept for potential future use)
     */
    private static boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        
        // Handle numeric comparison
        if (a instanceof Number && b instanceof Number) {
            double numA = ((Number) a).doubleValue();
            double numB = ((Number) b).doubleValue();
            return isEqual(numA, numB);
        }
        
        return a.equals(b);
    }
}