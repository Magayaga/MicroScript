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
    public class FunctionHigherOrder
    {
        public static List<object> Map(Func<object, object> fn, List<object> list)
        {
            var result = new List<object>();
            foreach (var item in list) result.Add(fn(item));
            return result;
        }

        public static List<object> Filter(Func<object, bool> predicate, List<object> list)
        {
            var result = new List<object>();
            foreach (var item in list) if (predicate(item)) result.Add(item);
            return result;
        }

        public static object Foldlt(Func<object, object, object> fn, object initial, List<object> list)
        {
            var acc = initial;
            foreach (var item in list) acc = fn(acc, item);
            return acc;
        }

        public static object Foldrt(Func<object, object, object> fn, object initial, List<object> list)
        {
            var acc = initial;
            for (int i = list.Count - 1; i >= 0; i--) acc = fn(list[i], acc);
            return acc;
        }

        public static List<object> ProcessMap(string operation, List<object> list)
        {
            if (operation.StartsWith("(*"))
            {
                var multiplier = double.Parse(operation.Substring(2, operation.Length - 3).Trim());
                return Map(x => MultiplyNumbers(x, multiplier), list);
            }
            if (operation.StartsWith("(+"))
            {
                var addend = double.Parse(operation.Substring(2, operation.Length - 3).Trim());
                return Map(x => AddNumbers(x, addend), list);
            }
            if (operation.StartsWith("(-"))
            {
                var subtrahend = double.Parse(operation.Substring(2, operation.Length - 3).Trim());
                return Map(x => SubtractNumbers(x, subtrahend), list);
            }
            if (operation.StartsWith("(/"))
            {
                var divisor = double.Parse(operation.Substring(2, operation.Length - 3).Trim());
                if (Math.Abs(divisor) < 0.0001) throw new Exception($"Division by zero in @map operation: {operation}");
                return Map(x => DivideNumbers(x, divisor), list);
            }

            throw new Exception($"Unsupported @map operation: {operation}");
        }

        private static object MultiplyNumbers(object a, double by) => a switch
        {
            int n => n * (int)by,
            long n => n * (long)by,
            float n => n * (float)by,
            double n => n * by,
            _ => throw new Exception($"Cannot multiply non-numeric type: {a.GetType().Name}")
        };

        private static object AddNumbers(object a, double by) => a switch
        {
            int n => n + (int)by,
            long n => n + (long)by,
            float n => n + (float)by,
            double n => n + by,
            _ => throw new Exception($"Cannot add to non-numeric type: {a.GetType().Name}")
        };

        private static object SubtractNumbers(object a, double by) => a switch
        {
            int n => n - (int)by,
            long n => n - (long)by,
            float n => n - (float)by,
            double n => n - by,
            _ => throw new Exception($"Cannot subtract from non-numeric type: {a.GetType().Name}")
        };

        private static object DivideNumbers(object a, double by) => a switch
        {
            int n => n % (int)by == 0 ? n / (int)by : (double)n / (int)by,
            long n => n % (long)by == 0 ? n / (long)by : (double)n / (long)by,
            float n => n / (float)by,
            double n => n / by,
            _ => throw new Exception($"Cannot divide non-numeric type: {a.GetType().Name}")
        };
    }
}
