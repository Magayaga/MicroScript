/**
 * MicroScript — The programming language
 * Copyright (c) 2025 Cyril John Magayaga
 * 
 * Converted from Java source.
 */
using System;

namespace com.magayaga.microscript
{
    public class NativeMath
    {
        public static double Sqrt(double value) => Math.Sqrt(value);
        public static double Cbrt(double value) => Math.Pow(value, 1.0 / 3.0);
        public static double PI() => Math.PI;
        public static double E() => Math.E;
        public static double TAU() => Math.PI * 2.0;
        public static double PHI() => (1.0 + Math.Sqrt(5.0)) / 2.0;
        public static double SILVERRATIO() => 1.0 + Math.Sqrt(2.0);
        public static double EULER() => 0.5772156649015329;
        public static double CATALAN() => 0.915965594177219;
        public static double APERY() => 1.202056903159594;
        public static double FEIGENBAUMDELTA() => 4.66920160910299;
        public static double FEIGENBAUMALPHA() => 2.50290787509589;
        public static double PLASTIC() => 1.3247179572447458;
        public static double TWINPRIME() => 0.6601618158468696;
        public static double Square(double value) => value * value;
        public static double Cube(double value) => value * value * value;
        public static double Abs(double value) => Math.Abs(value);
        public static double Log10(double value) => Math.Log10(value);
        public static double Log2(double value) => Math.Log(value, 2);
        public static double Log(double value) => Math.Log(value);
        public static double Sin(double value) => Math.Sin(value);
        public static double Cos(double value) => Math.Cos(value);
        public static double Tan(double value) => Math.Tan(value);
        public static double Asin(double value) => Math.Asin(value);
        public static double Acos(double value) => Math.Acos(value);
        public static double Atan(double value) => Math.Atan(value);
        public static double Atan2(double y, double x) => Math.Atan2(y, x);
        public static double Sinh(double value) => Math.Sinh(value);
        public static double Cosh(double value) => Math.Cosh(value);
        public static double Tanh(double value) => Math.Tanh(value);
        public static double Asinh(double value) => Math.Log(value + Math.Sqrt(value * value + 1.0));
        public static double Acosh(double value) => Math.Log(value + Math.Sqrt(value - 1.0) * Math.Sqrt(value + 1.0));
        public static double Atanh(double value) => 0.5 * Math.Log((1.0 + value) / (1.0 - value));
    }
}
