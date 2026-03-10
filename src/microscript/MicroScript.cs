/**
 * MicroScript — The programming language
 * Copyright (c) 2024-2026 Cyril John Magayaga
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
        private static readonly HashSet<string> ValidExtensions = new HashSet<string> { ".microscript", ".mus", ".micros" };

        public static void Main(string[] args)
        {
            if (args.Length < 1)
            {
                Console.WriteLine("Usage: MicroScript <file.microscript>");
                return;
            }

            var filePath = args[0];
            if (!HasValidExtension(filePath))
            {
                Console.Error.WriteLine("Error: File must have a valid MicroScript extension (.microscript, .mus, .micros)");
                Console.Error.WriteLine($"The file '{filePath}' does not have a recognized MicroScript extension.");
                return;
            }

            ExecuteScript(filePath);
        }

        private static bool HasValidExtension(string filePath)
        {
            var extension = Path.GetExtension(filePath);
            return !string.IsNullOrEmpty(extension) && ValidExtensions.Contains(extension);
        }

        private static void ExecuteScript(string filePath)
        {
            try
            {
                var scanner = new Scanner(filePath);
                var lines = scanner.ReadLines();

                var define = new Define();
                var preprocessedLines = define.Preprocess(lines);

                var parser = new Parser(preprocessedLines);
                parser.Parse();
            }
            catch (IOException e)
            {
                Console.Error.WriteLine($"Error reading file '{filePath}': {e.Message}");
            }
            catch (Exception e)
            {
                Console.Error.WriteLine($"Error executing script '{filePath}': {e.Message}");
            }
        }
    }
}
