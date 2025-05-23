# `MicroScript`

**MicroScript**, often abbreviated as **MUS** and **μs** is a high-level programming language. It was originally written in **Java**, **C**, and **C#** programming languages.

## Examples
### "Hello, World!" program
The following shows how a **"Hello, World!"** program is written in the MicroScript programming language:

```js
// "Hello, World!" program using MicroScript
function main() {
    console.write("Hello, World!");
    return 0;
}

main();
```

`fn` (new) and `function` (original) are functional programming. Then, new **"Hello, World!"** program is written in the MicroScript programming language:

```rust
// "Hello, World!" program using MicroScript
fn main() {
    console.write("Hello, World!");
}
```

### Arithmetic expressions
The following shows how to the addition, substraction, multiplication, and division program using MicroScript programming language:

```js
// Adding numbers
console.write(25 + 19);

// Subtracting numbers
console.write(36 - 14);

// Multiplying numbers
console.write(45 * 12);

// Dividing numbers
console.write(1028 / 16);
```

### Functions

The following shows how a **"Hello, World!"** program with function is written in MicroScript programming language:

```js
// Function to compute the "Hello, World!" program
function main() {
    console.write("Hello, World!");
    return 0;
};

main();
```

The following shows how a **square** function is written in MicroScript programming language:

```js
// Function to compute the square
function square(number: Float64) -> Float64 {
    return number * number;
}

// Main function
function main() {
    console.write(square(14));
};

main();
```

### Call main function

```js
main();
```

This executes the recursive `main` function, starting the program.

### Import modules and Libraries (Coming Soon)
You need to use the `import` keyword along with the desired module name like **Python** programming language.

```ts
// Import modules
import math

// Main function
function main() {
    console.write(math::PI);
    console.write(math::E);
    console.write(math::sqrt(16));
}

main();
```

### Variables
Explicitly-declared variables are created with the `var` keyword, and may include type annotations.

```csharp
// Main function
function main() {
    var username : String = "John";
    var number_e : Float64 = 2.7182818;
    var number_speedOfSound : Int64 = 343;
    console.write(username);
    console.write(number_e);
    console.write(number_speedOfSound);
}

main();
```

### Macros
Macros are defined using the `#define` preprocessor directive, like Object-like Macros and Function-like Macros. It is similar to **C** and **C++** programming languages.

```c
// Macros
#define NUMBER 9
#define NUMBER_PI 3.14159
#define NAME "John!"
#define SQUARE(x) (x * x)
#define MAX(a, b) ((a) > (b) ? (a) : (b))

// Main function
function main() {
    ...
}

main();
```

## Copyright

Copyright (c) 2024-2025 [Cyril John Magayaga](https://github.com/magayaga). Licensed under the [MIT license](LICENSE).
