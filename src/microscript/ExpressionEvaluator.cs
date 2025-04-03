/**
 * MicroScript — The programming language
 * Copyright (c) 2024-2025 Cyril John Magayaga
 * 
 * It was originally written in C# programming language.
 */
using System;

namespace com.magayaga.microscript
{
    public class ExpressionEvaluator
    {
        private readonly string expression;
        private readonly Environment environment;
        private int pos = -1;
        private int ch;

        public ExpressionEvaluator(string expression, Environment environment)
        {
            this.expression = expression;
            this.environment = environment;
        }

        public double Parse()
        {
            NextChar();
            var x = ParseExpression();
            if (pos < expression.Length) throw new Exception($"Unexpected: {(char)ch}");
            return x;
        }

        private void NextChar()
        {
            ch = (++pos < expression.Length) ? expression[pos] : -1;
        }

        private bool Eat(int charToEat)
        {
            while (ch == ' ') NextChar();
            if (ch == charToEat)
            {
                NextChar();
                return true;
            }
            return false;
        }

        private double ParseExpression()
        {
            var x = ParseTerm();
            while (true)
            {
                if (Eat('+')) x += ParseTerm();
                else if (Eat('-')) x -= ParseTerm();
                else return x;
            }
        }

        private double ParseTerm()
        {
            var x = ParseFactor();
            while (true)
            {
                if (Eat('*')) x *= ParseFactor();
                else if (Eat('/')) x /= ParseFactor();
                else return x;
            }
        }

        private double ParseFactor()
        {
            if (Eat('+')) return ParseFactor();
            if (Eat('-')) return -ParseFactor();

            double x;
            var startPos = this.pos;
            if (Eat('('))
            {
                x = ParseExpression();
                Eat(')');
            }

            else if ((ch >= '0' && ch <= '9') || ch == '.')
            {
                while ((ch >= '0' && ch <= '9') || ch == '.') NextChar();
                x = double.Parse(expression.Substring(startPos, this.pos - startPos));
            }

            else if (ch >= 'a' && ch <= 'z')
            {
                while (ch >= 'a' && ch <= 'z') NextChar();
                var func = expression.Substring(startPos, this.pos - startPos);
                if (Eat('('))
                {
                    x = ParseExpression();
                    Eat(')');
                }

                else
                {
                    var variable = environment.GetVariable(func);
                    if (variable == null)
                    {
                        throw new Exception($"Variable '{func}' not found.");
                    }
                    x = (double)variable;
                }
            }

            else
            {
                throw new Exception($"Unexpected: {(char)ch}");
            }

            if (Eat('^')) x = Math.Pow(x, ParseFactor());

            return x;
        }
    }
}