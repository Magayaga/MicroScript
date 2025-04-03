/**
 * MicroScript — The programming language
 * Copyright (c) 2024-2025 Cyril John Magayaga
 * 
 * It was originally written in C# programming language.
 */
using System;
using System.Collections.Generic;
using System.IO;

namespace com.magayaga.microscript
{
    public class MicroScript
    {
        public static void Main(string[] args)
        {
            if (args.Length < 1)
            {
                Console.WriteLine("Usage: MicroScript <file.microscript>");
                return;
            }

            var filePath = args[0];
            try
            {
                var lines = File.ReadAllLines(filePath);
                var parser = new Parser(new List<string>(lines));
                parser.Parse();
            }
            catch (IOException e)
            {
                Console.WriteLine($"Error reading file: {e.Message}");
            }
        }
    }
}