/**
 * MicroScript — The programming language
 * Copyright (c) 2025-2026 Cyril John Magayaga
 * 
 * Converted from Java source.
 */
using System;

namespace com.magayaga.microscript
{
    public class Cli
    {
        private const string Reset = "\u001B[0m";
        private const string Green = "\u001B[32;1m";
        private const string Blue = "\u001B[34;1m";
        private const string Version = "MicroScript v0.1.0";

        public static void PrintUsage()
        {
            Console.WriteLine($"{Green}Usage:{Reset} {Blue}microscript <command> [options]{Reset}");
            Console.WriteLine($"{Green}Options:{Reset}");
            Console.WriteLine($"  {Blue}--help{Reset}        Show help information");
            Console.WriteLine($"  {Blue}--version{Reset}     Show version information");
            Console.WriteLine($"{Green}Commands:{Reset}");
            Console.WriteLine($"  {Blue}run{Reset}           Run a MicroScript source file");
            Console.WriteLine($"  {Blue}about{Reset}         Show about information");
        }

        public static void PrintVersion() => Console.WriteLine($"{Blue}{Version}{Reset}");
    }
}
