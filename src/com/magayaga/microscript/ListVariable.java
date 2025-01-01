/**
 * MicroScript — The programming language
 * Copyright (c) 2024-2025 Cyril John Magayaga
 * 
 * It was originally written in Java programming language.
 */
package com.magayaga.microscript;

import java.util.ArrayList;

public class ListVariable extends ArrayList<Object> {
    public ListVariable() {
        super();
    }

    public ListVariable(String[] elements) {
        for (String element : elements) {
            this.add(element.trim());
        }
    }
}