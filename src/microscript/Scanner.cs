/**
 * MicroScript — The programming language
 * Copyright (c) 2024-2026 Cyril John Magayaga
 * 
 * Converted from Java source.
 */
using System.Collections.Generic;
using System.IO;
namespace com.magayaga.microscript
{
    public class Scanner
    {
        private readonly string filePath;

        public Scanner(string filePath)
        {
            this.filePath = filePath;
        }

        public List<string> ReadLines()
        {
            return new List<string>(File.ReadAllLines(filePath));
        }
    }
}
