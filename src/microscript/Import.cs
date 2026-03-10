/**
 * MicroScript — The programming language
 * Copyright (c) 2025-2026 Cyril John Magayaga
 * 
 * Converted from Java source.
 */
using System;
using System.Collections.Generic;

namespace com.magayaga.microscript
{
    public class Import
    {
        public delegate object? FunctionInterface(object?[] args);

        public interface IModule
        {
            void Register(Environment env);
        }

        private static readonly Dictionary<string, IModule> modules = new Dictionary<string, IModule>
        {
            { "math", new MathModule() },
            { "io", new IoModule() }
        };

        public static void ImportModule(string name, Environment env)
        {
            if (modules.TryGetValue(name, out var module))
            {
                module.Register(env);
                return;
            }

            throw new Exception($"Module not found: {name}");
        }

        public class MathModule : IModule
        {
            public void Register(Environment env)
            {
                env.SetVariable("math::numbers::pi", NativeMath.PI());
                env.SetVariable("math::numbers::e", NativeMath.E());
                env.SetVariable("math::sqrt", (FunctionInterface)(args => NativeMath.Sqrt(Convert.ToDouble(args[0]))));
                env.SetVariable("math::square", (FunctionInterface)(args => NativeMath.Square(Convert.ToDouble(args[0]))));
                env.SetVariable("math::cbrt", (FunctionInterface)(args => NativeMath.Cbrt(Convert.ToDouble(args[0]))));
                env.SetVariable("math::cube", (FunctionInterface)(args => NativeMath.Cube(Convert.ToDouble(args[0]))));
                env.SetVariable("math::abs", (FunctionInterface)(args => NativeMath.Abs(Convert.ToDouble(args[0]))));
            }
        }

        public class IoModule : IModule
        {
            public void Register(Environment env)
            {
                env.SetVariable("io::print", (FunctionInterface)(args =>
                {
                    if (args.Length == 0 || args[0] == null) return null;
                    if (args[0] is string s) NativeIo.Print(s);
                    else NativeIo.Print(Convert.ToInt32(args[0]));
                    return null;
                }));

                env.SetVariable("io::println", (FunctionInterface)(args =>
                {
                    if (args.Length == 0 || args[0] == null)
                    {
                        Console.WriteLine();
                        return null;
                    }
                    if (args[0] is string s) NativeIo.Println(s);
                    else NativeIo.Println(Convert.ToInt32(args[0]));
                    return null;
                }));
            }
        }
    }
}
