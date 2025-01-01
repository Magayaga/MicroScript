/**
 * MicroScript — The programming language
 * Copyright (c) 2024-2025 Cyril John Magayaga
 * 
 * It was originally written in Java programming language.
 */
package com.magayaga.microscript;

import java.util.List;

public class Function {
    private final String name;
    private final List<String> parameters;
    private final List<String> body;

    public Function(String name, List<String> parameters, List<String> body) {
        this.name = name;
        this.parameters = parameters;
        this.body = body;
    }

    public String getName() {
        return name;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public List<String> getBody() {
        return body;
    }
}
