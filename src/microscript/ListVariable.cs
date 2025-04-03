/**
 * MicroScript — The programming language
 * Copyright (c) 2024-2025 Cyril John Magayaga
 * 
 * It was originally written in C# programming language.
 */
using System.Collections.Generic;

namespace com.magayaga.microscript
{
    public class ListVariable : List<object>
    {
        public ListVariable() : base() { }

        public ListVariable(string[] elements)
        {
            foreach (var element in elements)
            {
                this.Add(element.Trim());
            }
        }
    }
}