/**
 * MicroScript — The programming language
 * Copyright (c) 2024-2025 Cyril John Magayaga
 * 
 * It was originally written in Java programming language.
 */
package com.magayaga.microscript;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Arrays;

public class MicroScript {
    // List of valid MicroScript file extensions
    private static final List<String> VALID_EXTENSIONS = Arrays.asList(
        ".microscript", ".mus", ".μs", ".📜"
    );
    
    public static void main(String[] args) {
        if (args.length < 2 || !args[0].equals("run")) {
            System.out.println("Usage: java com.microscript.MicroScript run <file.microscript>");
            System.out.println("Supported extensions: .microscript, .mus, .μs, .📜");
            return;
        }
        
        String filePath = args[1];
        
        // Check if file has a valid MicroScript extension
        boolean hasValidExtension = false;
        for (String ext : VALID_EXTENSIONS) {
            if (filePath.endsWith(ext)) {
                hasValidExtension = true;
                break;
            }
        }
        
        if (!hasValidExtension) {
            System.out.println("Error: File must have a valid MicroScript extension (.microscript, .mus, .μs, or .📜)");
            System.out.println("The file '" + filePath + "' does not have a recognized MicroScript extension.");
            return;
        }
        
        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            Parser parser = new Parser(lines);
            parser.parse();
        }

        catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }
}
