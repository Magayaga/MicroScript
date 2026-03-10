/**
 * MicroScript — The programming language
 * Copyright (c) 2024-2026 Cyril John Magayaga
 * 
 * Converted from Java source.
 */
using System;
namespace com.magayaga.microscript
{
    public class PanicException : Exception
    {
        public PanicException(string message) : base(message) { }
    }
}
