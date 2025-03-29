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
    private final List<Parameter> parameters;
    private final String returnType;
    private final List<String> body;

    public Function(String name, List<Parameter> parameters, String returnType, List<String> body) {
        this.name = name;
        this.parameters = parameters;
        this.returnType = returnType;
        this.body = body;
    }

    public String getName() {
        return name;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public String getReturnType() {
        return returnType;
    }

    public List<String> getBody() {
        return body;
    }
}
