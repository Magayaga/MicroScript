/**
 * MicroScript — The programming language
 * Copyright (c) 2024-2026 Cyril John Magayaga
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
    
    public ListVariable(Object[] elements) {
        for (Object element : elements) {
            this.add(element);
        }
    }
    
    public Object get(int index) {
        return super.get(index);
    }
}