/**
 * MicroScript — The programming language
 * Copyright (c) 2024-2025 Cyril John Magayaga
 * 
 * It was originally written in C# programming language.
 */
namespace com.magayaga.microscript
{
    public class Loop
    {
        public static void ExecuteWhile(Environment environment, string condition, string body)
        {
            var executor = new Executor(environment);
            while ((bool)executor.Evaluate(condition))
            {
                executor.Execute(body);
            }
        }
    }
}