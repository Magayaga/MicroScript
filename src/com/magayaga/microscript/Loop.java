/**
 * MicroScript — The programming language
 * Copyright (c) 2024-2025 Cyril John Magayaga
 * 
 * It was originally written in Java programming language.
 */
package com.magayaga.microscript;

public class Loop {
    public static void executeWhile(Environment environment, String condition, String body) {
        Executor executor = new Executor(environment);
        while ((boolean) executor.evaluate(condition)) {
            executor.execute(body);
        }
    }
}