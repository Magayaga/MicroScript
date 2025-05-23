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

public class Scanner {
    private final String filePath;

    public Scanner(String filePath) {
        this.filePath = filePath;
    }

    public List<String> readLines() throws IOException {
        return Files.readAllLines(Paths.get(filePath));
    }
} 