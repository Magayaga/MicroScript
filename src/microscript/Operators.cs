/**
 * MicroScript — The programming language
 * Copyright (c) 2024-2025 Cyril John Magayaga
 * 
 * It was originally written in C# programming language.
 */
namespace com.magayaga.microscript
{
    public class Operators
    {
        public static int Increment(Environment environment, string varName)
        {
            var value = environment.GetVariable(varName);
            if (value is int intValue)
            {
                intValue++;
                environment.SetVariable(varName, intValue);
                return intValue;
            }

            else
            {
                throw new Exception($"Type error: {varName} is not an Integer.");
            }
        }

        public static int Decrement(Environment environment, string varName)
        {
            var value = environment.GetVariable(varName);
            if (value is int intValue)
            {
                intValue--;
                environment.SetVariable(varName, intValue);
                return intValue;
            }

            else
            {
                throw new Exception($"Type error: {varName} is not an Integer.");
            }
        }
    }
}