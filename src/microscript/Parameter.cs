/**
 * MicroScript — The programming language
 * Copyright (c) 2024-2025 Cyril John Magayaga
 * 
 * It was originally written in C# programming language.
 */
namespace com.magayaga.microscript
{
    public class Parameter
    {
        private readonly string name;
        private readonly string type;

        public Parameter(string name, string type)
        {
            this.name = name;
            this.type = type;
        }

        public string GetName()
        {
            return name;
        }

        public new string GetType()
        {
            return type;
        }
    }
}