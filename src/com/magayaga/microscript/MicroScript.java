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

public class MicroScript {
    public static void main(String[] args) {
        if (args.length < 2 || !args[0].equals("run")) {
            System.out.println("Usage: java com.microscript.MicroScript run <file.mus>");
            return;
        }

        String filePath = args[1];
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
