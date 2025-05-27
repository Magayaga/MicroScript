/**
 * MicroScript — The programming language
 * Copyright (c) 2024-2025 Cyril John Magayaga
 * 
 * It was originally written in Java programming language.
 */
package com.magayaga.microscript;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public class MicroScript {
    // Use Set for O(1) lookup instead of List with O(n) iteration
    private static final Set<String> VALID_EXTENSIONS = Set.of(
        ".microscript", ".mus", ".micros"
    );
    
    // Constants for better maintainability
    private static final String RUN_COMMAND = "run";
    
    public static void main(String[] args) {
        // Handle CLI commands early return pattern
        if (shouldDelegateToCli(args)) {
            Cli.main(args);
            return;
        }

        if (!isValidRunCommand(args)) {
            Cli.printUsage();
            return;
        }
        
        String filePath = args[1];
        
        // Validate file extension with improved efficiency
        if (!hasValidExtension(filePath)) {
            printExtensionError(filePath);
            return;
        }
        
        // Execute MicroScript file
        executeScript(filePath);
    }
    
    /**
     * Determines if the command should be delegated to CLI handler
     */
    private static boolean shouldDelegateToCli(String[] args) {
        if (args.length == 0) {
            return true;
        }
        
        String firstArg = args[0];
        return "--help".equals(firstArg) || 
               "--version".equals(firstArg) || 
               "about".equals(firstArg);
    }
    
    /**
     * Validates if the command is a proper run command with required arguments
     */
    private static boolean isValidRunCommand(String[] args) {
        return args.length >= 2 && RUN_COMMAND.equals(args[0]);
    }
    
    /**
     * Efficiently checks if file has valid MicroScript extension using Set lookup
     * Time complexity: O(1) average case vs O(n) with List iteration
     */
    private static boolean hasValidExtension(String filePath) {
        // Find the last dot in the filename
        int lastDotIndex = filePath.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filePath.length() - 1) {
            return false; // No extension or ends with dot
        }
        
        String extension = filePath.substring(lastDotIndex);
        return VALID_EXTENSIONS.contains(extension);
    }
    
    /**
     * Prints formatted error message for invalid file extensions
     */
    private static void printExtensionError(String filePath) {
        System.err.println("Error: File must have a valid MicroScript extension (.microscript, .mus, .micros)");
        System.err.println("The file '" + filePath + "' does not have a recognized MicroScript extension.");
    }
    
    /**
     * Executes the MicroScript file with proper error handling
     */
    private static void executeScript(String filePath) {
        try {
            // Create Scanner object
            Scanner scanner = new Scanner(filePath);
            
            // Read and preprocess lines
            List<String> lines = scanner.readLines();
            
            // Preprocess macros
            Define define = new Define();
            List<String> preprocessedLines = define.preprocess(lines);
            
            // Parse and execute
            Parser parser = new Parser(scanner);
            parser.parse();
            
        } catch (IOException e) {
            System.err.println("Error reading file '" + filePath + "': " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Error executing script '" + filePath + "': " + e.getMessage());
        }
    }
}
