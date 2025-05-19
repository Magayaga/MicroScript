/**
 * MicroScript — The programming language
 * Copyright (c) 2025 Cyril John Magayaga
 * 
 * It was originally written in Java programming language.
 */
package com.magayaga.microscript;

import java.util.List;

public class ArrowFunction extends Function {
    private final String body;
    private final boolean isExpression;

    public ArrowFunction(String name, List<Parameter> parameters, String returnType, String body, boolean isExpression) {
        super(name, parameters, returnType, null); // Pass null for body since we'll override it
        this.body = body;
        this.isExpression = isExpression;
    }

    // Override to handle the body as a string instead of a list
    @Override
    public List<String> getBody() {
        // For expression bodied functions, wrap the expression in a return statement
        if (isExpression) {
            return List.of("return " + body + ";");
        } else {
            // For block bodied functions, split the body into lines
            return List.of(body.split("\\r?\\n"));
        }
    }
    
    public String getRawBody() {
        return body;
    }
    
    public boolean isExpression() {
        return isExpression;
    }
}
