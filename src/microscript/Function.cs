/**
 * MicroScript — The programming language
 * Copyright (c) 2024-2025 Cyril John Magayaga
 * 
 * It was originally written in C# programming language.
 */
using System.Collections.Generic;

namespace com.magayaga.microscript
{
    public class Function
    {
        private readonly string name;
        private readonly List<Parameter> parameters;
        private readonly string returnType;
        private readonly List<string> body;

        public Function(string name, List<Parameter> parameters, string returnType, List<string> body)
        {
            this.name = name;
            this.parameters = parameters;
            this.returnType = returnType;
            this.body = body;
        }

        public string GetName()
        {
            return name;
        }

        public List<Parameter> GetParameters()
        {
            return parameters;
        }

        public string GetReturnType()
        {
            return returnType;
        }

        public List<string> GetBody()
        {
            return body;
        }
    }
}