/**
 * MicroScript — The programming language
 * Copyright (c) 2025 Cyril John Magayaga
 * 
 * Converted from Java source.
 */
using System;

namespace com.magayaga.microscript
{
    public class NativeIo
    {
        public static void Print(string message) => Console.Write(message);
        public static void Print(int code) => Console.Write((char)code);
        public static void Println(string message) => Console.WriteLine(message);
        public static void Println(int code) => Console.WriteLine((char)code);
    }
}
