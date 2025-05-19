/**
 * MicroScript — The programming language
 * Copyright (c) 2024-2025 Cyril John Magayaga
 * 
 * It was originally written in Java programming language.
 */
package com.magayaga.microscript;

import java.util.List;
import java.util.ArrayList;

public class ArrowFunction extends Function {
    private final String expression;
    private final boolean isExpressionBody;

    public ArrowFunction(String name, List<Parameter> parameters, String returnType, String expression, boolean isExpressionBody) {
        super(name, parameters, returnType, createBody(expression, returnType, isExpressionBody));
        this.expression = expression;
        this.isExpressionBody = isExpressionBody;
    }

    private static List<String> createBody(String expression, String returnType, boolean isExpressionBody) {
        List<String> body = new ArrayList<>();
        if (isExpressionBody) {
            body.add("return " + expression + ";");
        } else {
            // For block bodies, the expression already contains the full body content
            // We just need to make sure it has a return statement if needed
            String[] lines = expression.split(";");
            for (int i = 0; i < lines.length; i++) {
                String line = lines[i].trim();
                if (!line.isEmpty()) {
                    // Add semicolon except for lines ending with {
                    if (!line.endsWith("{") && !line.endsWith("}") && i < lines.length - 1) {
                        body.add(line + ";");
                    } else {
                        body.add(line);
                    }
                }
            }
        }
        return body;
    }

    public String getExpression() {
        return expression;
    }

    public boolean isExpressionBody() {
        return isExpressionBody;
    }
}
