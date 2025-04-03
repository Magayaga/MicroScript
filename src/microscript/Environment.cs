/**
 * MicroScript — The programming language
 * Copyright (c) 2024-2025 Cyril John Magayaga
 * 
 * It was originally written in C# programming language.
 */
using System;
using System.Collections.Generic;

namespace com.magayaga.microscript
{
    public class Environment
    {
        private readonly Dictionary<string, object> variables = new Dictionary<string, object>();
        private readonly Dictionary<string, Function> functions = new Dictionary<string, Function>();
        private readonly Environment? parent;

        public Environment()
        {
            this.parent = null;
        }

        public Environment(Environment parent)
        {
            this.parent = parent ?? throw new ArgumentNullException(nameof(parent));
        }

        public void SetVariable(string name, object value)
        {
            variables[name] = value;
        }

        public object? GetVariable(string name)
        {
            if (variables.TryGetValue(name, out var value))
            {
                return value;
            }
            if (parent != null)
            {
                return parent.GetVariable(name);
            }
            return null;
        }

        public void DefineFunction(Function function)
        {
            functions[function.GetName()] = function;
        }

        public Function? GetFunction(string name)
        {
            if (functions.TryGetValue(name, out var function))
            {
                return function;
            }
            if (parent != null)
            {
                return parent.GetFunction(name);
            }
            return null;
        }
    }
}